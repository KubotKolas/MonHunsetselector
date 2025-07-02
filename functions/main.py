# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`



# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).


# initialize_app()
#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")

from firebase_functions import https_fn
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app
import re
import os
import requests
from flask import redirect
from firebase_functions import https_fn
from firebase_admin import initialize_app, auth
from openid.consumer import consumer

set_global_options(max_instances=10, region="europe-west1")


# Initialize the Firebase Admin SDK
initialize_app()

# This is the URL of our function, which we will also use as the realm.
# For security, OpenID requires the return_to URL to be within the realm.
# IMPORTANT: Replace this with your actual function URL after deployment.
REALM = "https://verifysteam-o7vztoop6q-ew.a.run.app/"
RETURN_TO_URL = f"{REALM}verifySteam/"



@https_fn.on_request(secrets=["STEAM_KEY"])
def verifySteam(request: https_fn.Request) -> https_fn.Response:
    """
    This function is triggered when Steam redirects the user back to our URL.
    It verifies the OpenID response and creates a Firebase custom token.
    """

    STEAM_API_KEY = os.environ.get("STEAM_KEY")
    # Create an OpenID consumer session
    oidconsumer = consumer.Consumer(session={}, store=None)

    # Verify the OpenID assertion from Steam using the full request URL
    info = oidconsumer.complete(request.args, RETURN_TO_URL)

    # 1. Handle errors or failed authentication
    if info.status != consumer.SUCCESS:
        print(f"Steam authentication failed. Status: {info.status}, Message: {info.message}")
        return redirect("monhunsetselector://auth?error=failed")

    # 2. Extract the SteamID from the Claimed Identifier
    steam_id_regex = re.compile(r"https?://steamcommunity\.com/openid/id/(\d+)")
    match = steam_id_regex.match(info.identity_url)

    if not match:
        print(f"Could not extract SteamID from claimed identifier: {info.identity_url}")
        return redirect("monhunsetselector://auth?error=bad_id")

    steam_id = match.group(1)
    uid = f"steam:{steam_id}"  # Create a unique Firebase UID

    # --- Fetch user data from Steam Web API ---
    player_data = {}
    if not STEAM_API_KEY:
        print("Warning: STEAM_API_KEY secret not set or not accessible.")
    else:
        try:
            api_url = f"https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key={STEAM_API_KEY}&steamids={steam_id}"
            response = requests.get(api_url)
            response.raise_for_status()
            data = response.json()
            if data["response"]["players"]:
                player = data["response"]["players"][0]
                player_data["name"] = player.get("personaname", "Steam User")
                player_data["picture"] = player.get("avatarfull", "")
                player_data["profile_url"] = player.get("profileurl", "")
        except requests.exceptions.RequestException as e:
            print(f"Error fetching player summary from Steam API: {e}")
        except (KeyError, IndexError) as e:
            print(f"Error parsing Steam API response: {e}")

    # --- Create Custom Token (remains the same) ---
    try:
        custom_claims = {
            "steam_id": steam_id,
            "name": player_data.get("name", "Steam User"),
            "picture": player_data.get("picture", ""),
            "profile_url": player_data.get("profile_url", "")
        }
        custom_token = auth.create_custom_token(uid, custom_claims)

        # 4. Success! Redirect back to the mobile app with the token.
        print(f"Successfully created token for SteamID: {steam_id}")
        return redirect(f"monhunsetselector://auth?token={custom_token.decode('utf-8')}")
    except Exception as e:
        print(f"Error creating custom token: {e}")
        return redirect("monhunsetselector://auth?error=token_creation_failed")
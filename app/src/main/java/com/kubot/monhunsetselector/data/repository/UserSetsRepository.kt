package com.kubot.monhunsetselector.data.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kubot.monhunsetselector.data.models.UserSet
import kotlinx.coroutines.tasks.await

object UserSetsRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var cachedSets: List<UserSet>? = null

    private fun setsCollection() = auth.currentUser?.uid?.let { userId ->
        db.collection("users").document(userId).collection("saved_sets")
    }

    /**
     * Gets all sets for the currently logged-in user, using a cache.
     * @param forceRefresh If true, it will ignore the cache and fetch from Firestore.
     */
    suspend fun getMySets(forceRefresh: Boolean = false): List<UserSet> {
        // If we are not forcing a refresh and the cache is not null, return the cache!
        if (!forceRefresh && cachedSets != null) {
            println("SETS_CACHE: Returning cached sets.")
            return cachedSets!!
        }

        println("SETS_CACHE: Fetching sets from Firestore.")
        val collection = setsCollection() ?: return emptyList()
        return try {
            val snapshot = collection
                .orderBy("lastModified", Query.Direction.DESCENDING)
                .get()
                .await()

            val newSets = snapshot.toObjects(UserSet::class.java)
            // Store the newly fetched list in our cache
            cachedSets = newSets
            newSets
        } catch (e: Exception) {
            println("Error fetching sets: $e")
            emptyList()
        }
    }

    /**
     * When saving or deleting a set, we MUST clear the cache so the next
     * call to getMySets() will fetch the updated list.
     */
    private fun invalidateCache() {
        cachedSets = null
        println("SETS_CACHE: Cache invalidated.")
    }

    // Save or update a set. This handles the "queued" saving you wanted.
    suspend fun saveSet(set: UserSet) {
        val collection = setsCollection() ?: return
        if (set.id.isBlank()) {
            // Creating a new set
            collection.add(set).await()
        } else {
            // Updating an existing set
            collection.document(set.id).set(set).await()
        }
        // Invalidate the cache after a successful write
        invalidateCache()
    }

    // Get a single set by its ID to edit it
    suspend fun getSetById(setId: String): UserSet? {
        val document = setsCollection()?.document(setId)?.get()?.await()
        return document?.toObject(UserSet::class.java)?.apply { id = document.id }
    }

    suspend fun deleteSet(setId: String) {
        setsCollection()?.document(setId)?.delete()?.await()
        // Invalidate the cache after a successful write
        invalidateCache()
    }
}
package com.example.paintingrecognition.viewModels

import com.example.paintingrecognition.models.ScanResult
import io.reactivex.rxjava3.subjects.PublishSubject

class ScanViewModel {

    var scanResultSubject = PublishSubject.create<MutableList<ScanResult>>()

    /**
     * Saving photo in db.
     */
    fun savePhoto() {

    }

    /**
     * Emitting the result fom db.
     */
    fun emmitResult() {
        val items = mutableListOf(
            ScanResult("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 1", "action", "Leonardo", 35.5f),
            ScanResult("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 2", "drama", "michelangelo", 93.1f),
            ScanResult("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 3", "horror", "NFT", 44.2f),
            ScanResult("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 4", "drama", "Unknown", 11.5f),
            ScanResult("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 5", "scifi", "Le", 72.5f),
        )
        items.sortByDescending { it.resemblance }
        scanResultSubject.onNext(items)
    }
}
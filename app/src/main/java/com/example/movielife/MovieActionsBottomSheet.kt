package com.example.movielife

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class MovieActionsBottomSheet(
    private var movieId: Int,
    private val onActionsConfirmed: (watchlist: Boolean, watched: Boolean, comment: String, rating: Float) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var checkWatchlist: CheckBox
    private lateinit var checkWatched: CheckBox
    private lateinit var commentEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var btnConfirm: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_movie_actions_bottom_sheet, container, false)

        checkWatchlist = view.findViewById(R.id.checkbox_watchlist)
        checkWatched = view.findViewById(R.id.checkbox_watched)
        commentEditText = view.findViewById(R.id.edit_comment)
        ratingBar = view.findViewById(R.id.rating_bar)
        btnConfirm = view.findViewById(R.id.btn_confirm)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val movieIdStr = movieId.toString()

        if (uid != null) {
            val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

            // Marcar checkWatchlist si ya está en la watchlist
            database.child("watchlistPeliculas").get().addOnSuccessListener { snapshot ->
                val lista =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                if (lista.contains(movieIdStr)) {
                    checkWatchlist.isChecked = true
                }
            }

            // Marcar checkWatched si ya está en películas vistas
            database.child("peliculasVistas").get().addOnSuccessListener { snapshot ->
                val lista =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                if (lista.contains(movieIdStr)) {
                    checkWatched.isChecked = true
                }
            }
        }

        btnConfirm.setOnClickListener {
            val watchlist = checkWatchlist.isChecked
            val watched = checkWatched.isChecked
            val comment = commentEditText.text.toString()
            val rating = ratingBar.rating

            if (uid != null) {
                val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

                // Actualizar watchlist
                database.child("watchlistPeliculas").get().addOnSuccessListener { snapshot ->
                    val listaActual =
                        snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                            ?: listOf()
                    val nuevaLista = if (watchlist) {
                        if (!listaActual.contains(movieIdStr)) listaActual + movieIdStr else listaActual
                    } else {
                        listaActual - movieIdStr
                    }
                    database.child("watchlistPeliculas").setValue(nuevaLista)
                }

                // Actualizar películas vistas
                database.child("peliculasVistas").get().addOnSuccessListener { snapshot ->
                    val listaActual =
                        snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                            ?: listOf()
                    val nuevaLista = if (watched) {
                        if (!listaActual.contains(movieIdStr)) listaActual + movieIdStr else listaActual
                    } else {
                        listaActual - movieIdStr
                    }
                    database.child("peliculasVistas").setValue(nuevaLista)
                }
            }

            onActionsConfirmed(watchlist, watched, comment, rating)
            dismiss()
        }

        return view
    }
}

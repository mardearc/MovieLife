package com.example.movielife

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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

            // Guardar publicación solo si existe comentario y rating
            if(comment.isNotEmpty() && rating.toString().isNotEmpty()){
                savePost(movieId, comment, rating.toDouble())
            }


            onActionsConfirmed(watchlist, watched, comment, rating)
            dismiss()
        }

        return view
    }

    private fun savePost(peliculaId:Int, comentario:String, valoracion: Double) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.getReference("postspeliculas")

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val post = PostPelicula(
            peliculaId = peliculaId,
            uid = uid,
            comentario = comentario,
            valoracion = valoracion
        )

        val nuevoPostRef = postsRef.push()
        nuevoPostRef.setValue(post).addOnSuccessListener {
            Log.d("Firebase", "Post guardado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al guardar el post", e)
        }

        // Guardo una referencia al post para evitar duplicados
        val usuarioPostsRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .child("postspeliculas")
        usuarioPostsRef.child(nuevoPostRef.key!!).setValue(true)

        //Guardo una referencia del post para cada película
        val peliculaPostsRef = FirebaseDatabase.getInstance()
            .getReference("peliculas")
            .child(peliculaId.toString())
            .child("postspeliculas")

        peliculaPostsRef.child(nuevoPostRef.key!!).setValue(true)


    }
}

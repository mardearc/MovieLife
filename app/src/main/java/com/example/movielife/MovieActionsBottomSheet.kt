package com.example.movielife

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class MovieActionsBottomSheet(
    private var movieId: Int,
    private var posterPath: String,
    private val onActionsConfirmed: (watchlist: Boolean, watched: Boolean, comment: String, rating: Float, tipo: String) -> Unit
) : BottomSheetDialogFragment() {


    private lateinit var commentEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var btnConfirm: Button
    private lateinit var ivWatchlist: ImageView
    private lateinit var ivVistas: ImageView
    private var isInWatchlist = false
    private var isWatched = false


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_movie_actions_bottom_sheet, container, false)

        commentEditText = view.findViewById(R.id.edit_comment)
        ratingBar = view.findViewById(R.id.rating_bar)
        btnConfirm = view.findViewById(R.id.btn_confirm)
        ivWatchlist = view.findViewById(R.id.ivWatchlist)
        ivVistas = view.findViewById(R.id.ivVistas)

        ivWatchlist.setOnClickListener {
            isInWatchlist = !isInWatchlist
            updateWatchlistUI()
        }

        ivVistas.setOnClickListener {
            isWatched = !isWatched
            updateWatchedUI()
        }


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val movieIdStr = movieId.toString()

        if (uid != null) {
            val database = FirebaseDatabase.getInstance().reference.child("usuarios").child(uid)

            // Inicializar estado de Watchlist
            database.child("watchlistPeliculas").get().addOnSuccessListener { snapshot ->
                val lista =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                isInWatchlist = lista.contains(movieIdStr)
                updateWatchlistUI()
            }

            // Inicializar estado de Películas Vistas
            database.child("peliculasVistas").get().addOnSuccessListener { snapshot ->
                val lista =
                    snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                isWatched = lista.contains(movieIdStr)
                updateWatchedUI()
            }
        }

        btnConfirm.setOnClickListener {
            val watchlist = isInWatchlist
            val watched = isWatched
            val comment = commentEditText.text.toString()
            val rating = ratingBar.rating
            val tipo = "pelicula"

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
            if (comment.isNotEmpty() && rating.toString().isNotEmpty()) {
                savePost(movieId, comment, rating.toDouble())
                Toast.makeText(requireContext(), "Publicación guardada", Toast.LENGTH_SHORT).show()
            }


            onActionsConfirmed(watchlist, watched, comment, rating, tipo)
            dismiss()
        }

        return view
    }

    private fun savePost(peliculaId: Int, comentario: String, valoracion: Double) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.getReference("postspeliculas")

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val post = Post(
            peliculaId = peliculaId,
            uid = uid,
            comentario = comentario,
            valoracion = valoracion,
            posterPath = posterPath,
            tipo = "pelicula"
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

    private fun updateWatchlistUI() {
        val color = if (isInWatchlist)
            ContextCompat.getColor(requireContext(), R.color.green_principal)
        else
            ContextCompat.getColor(requireContext(), R.color.black)

        ivWatchlist.setColorFilter(color)
    }

    private fun updateWatchedUI() {
        val color = if (isWatched)
            ContextCompat.getColor(requireContext(), R.color.green_principal)
        else
            ContextCompat.getColor(requireContext(), R.color.black)

        ivVistas.setColorFilter(color)
    }

}

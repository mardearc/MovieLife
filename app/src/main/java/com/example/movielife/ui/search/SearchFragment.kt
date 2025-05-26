package com.example.movielife.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.example.movielife.R
import com.example.movielife.ui.adapters.SearchUserAdapter
import com.example.movielife.model.User
import com.example.movielife.databinding.FragmentSearchBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var adapter: SearchUserAdapter
    private var listaUsuarios: List<User> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchUserAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        obtenerUsuarios()
    }


    private fun obtenerUsuarios() {
        val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuariosCargados = mutableListOf<User>()

                for (userSnapshot in snapshot.children) {
                    val usuario = userSnapshot.getValue(User::class.java)
                    if (usuario != null) {
                        usuariosCargados.add(usuario)
                        Log.d("SearchFragment", "Usuario: ${usuario.nombreUsuario}")
                    } else {
                        Log.w("SearchFragment", "Documento inválido: ${userSnapshot.key}")
                    }
                }

                listaUsuarios = usuariosCargados
                adapter.actualizarLista(listaUsuarios)
                Log.d("SearchFragment", "Usuarios cargados desde RTDB: ${listaUsuarios.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchFragment", "Error al leer desde RTDB", error.toException())
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_busqueda, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Buscar usuarios..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText?.trim().orEmpty()
                val filtrados = listaUsuarios.filter {
                    it.nombreUsuario.contains(texto, ignoreCase = true)
                }
                adapter.actualizarLista(filtrados)
                return true
            }
        })
    }

}
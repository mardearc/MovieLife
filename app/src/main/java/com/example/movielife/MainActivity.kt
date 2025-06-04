package com.example.movielife

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.movielife.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Flujo de fragments
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_movies, R.id.nav_series, R.id.nav_watchlist, R.id.nav_search, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        val database = FirebaseDatabase.getInstance("https://movielife-9f648-default-rtdb.europe-west1.firebasedatabase.app")
        val databaseRef = database.getReference("usuarios")

        val userRef = databaseRef.child(userId!!)

        // Pasar id al ir a Mi Perfil
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val bundle = Bundle().apply {
                        putString("uid", userId)
                    }
                    navController.navigate(R.id.nav_profile, bundle)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    // Para los demás, usar navegación por defecto
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawers()
                    true
                }
            }
        }

        // Cargar imagen y nombre de ese usuario
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val nombre = dataSnapshot?.child("nombreUsuario")?.value.toString()
                var foto = dataSnapshot?.child("fotoPerfil")?.value.toString()

                val navigationView = findViewById<NavigationView>(R.id.nav_view)

                val headerView = navigationView.getHeaderView(0)

                val nombreUsuario = headerView.findViewById<TextView>(R.id.textViewUsuarioLateral)
                val fotoPerfil = headerView.findViewById<ImageView>(R.id.imageViewMenuLateral)

                // Obtener el ID del recurso a partir del nombre
                val resId = resources.getIdentifier(foto, "drawable", packageName)

                // Cargar la imagen en el ImageView
                nombreUsuario.text = nombre
                fotoPerfil.setImageResource(resId)

            } else {
                Log.e("Firebase", "Error al obtener los datos", task.exception)
            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
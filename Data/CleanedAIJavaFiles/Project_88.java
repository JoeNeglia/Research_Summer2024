// MainActivity.kt
package com.example.ecosaver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        updateUI(currentUser)

        val loginBtn: Button = findViewById(R.id.login_btn)
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val registerBtn: Button = findViewById(R.id.register_btn)
        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val logoutBtn: Button = findViewById(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            auth.signOut()
            updateUI(null)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update UI based on login status
    }
}

// LoginActivity.kt
package com.example.ecosaver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)

        val loginBtn: Button = findViewById(R.id.login_btn)
        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // Handle error
            }
        }
    }
}

// RegisterActivity.kt
package com.example.ecosaver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)

        val registerBtn: Button = findViewById(R.id.register_btn)
        registerBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // Handle error
            }
        }
    }
}

// DashboardActivity.kt
package com.example.ecosaver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.ecosaver.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        loadFragment(HomeFragment())

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_tracker -> selectedFragment = CarbonFootprintFragment()
                R.id.nav_tips -> selectedFragment = TipsFragment()
                R.id.nav_community -> selectedFragment = CommunityFragment()
                R.id.nav_news -> selectedFragment = NewsFragment()
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment)
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

// HomeFragment.kt
package com.example.ecosaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ecosaver.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}

// CarbonFootprintFragment.kt
package com.example.ecosaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ecosaver.R

class CarbonFootprintFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carbon_footprint, container, false)
    }
}

// TipsFragment.kt
package com.example.ecosaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ecosaver.R

class TipsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }
}

// CommunityFragment.kt
package com.example.ecosaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecosaver.R
import com.example.ecosaver.adapters.CommunityPostAdapter
import com.example.ecosaver.models.CommunityPost

class CommunityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dummyPosts = listOf(
            CommunityPost("User1", "Check out my new solar panels!"),
            CommunityPost("User2", "I planted 10 trees today!"),
            CommunityPost("User3", "Just biked to work for the first time!")
        )

        recyclerView.adapter = CommunityPostAdapter(dummyPosts)
        return view
    }
}

// NewsFragment.kt
package com.example.ecosaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecosaver.R
import com.example.ecosaver.adapters.NewsArticleAdapter
import com.example.ecosaver.models.NewsArticle

class NewsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dummyArticles = listOf(
            NewsArticle("New Green Technology Innovations", "Today"),
            NewsArticle("How to Reduce Your Carbon Footprint", "Yesterday"),
            NewsArticle("The Impact of Climate Change", "Last Week")
        )

        recyclerView.adapter = NewsArticleAdapter(dummyArticles)
        return view
    }
}

// CommunityPostAdapter.kt
package com.example.ecosaver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecosaver.R
import com.example.ecosaver.models.CommunityPost

class CommunityPostAdapter(private val postList: List<CommunityPost>) :
    RecyclerView.Adapter<CommunityPostAdapter.CommunityPostViewHolder>() {

    class CommunityPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val postTextView: TextView = itemView.findViewById(R.id.post_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return CommunityPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPostViewHolder, position: Int) {
        val post = postList[position]
        holder.usernameTextView.text = post.username
        holder.postTextView.text = post.text
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}

// NewsArticleAdapter.kt
package com.example.ecosaver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecosaver.R
import com.example.ecosaver.models.NewsArticle

class NewsArticleAdapter(private val articleList: List<NewsArticle>) :
    RecyclerView.Adapter<NewsArticleAdapter.NewsArticleViewHolder>() {

    class NewsArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_article, parent, false)
        return NewsArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val article = articleList[position]
        holder.titleTextView.text = article.title
        holder.dateTextView.text = article.date
    }

    override fun getItemCount(): Int {
        return articleList.size
    }
}

// CommunityPost.kt
package com.example.ecosaver.models

data class CommunityPost(
    val username: String,
    val text: String
)

// NewsArticle.kt
package com.example.ecosaver.models

data class NewsArticle(
    val title: String,
    val date: String
)
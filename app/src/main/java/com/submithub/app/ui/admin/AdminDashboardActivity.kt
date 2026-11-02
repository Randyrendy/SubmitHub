package com.submithub.app.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.content.Intent
import com.submithub.app.ui.settings.SettingsActivity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.submithub.app.data.Repository
import com.submithub.app.data.entity.CodeEntity
import com.submithub.app.data.entity.UserEntity
import com.submithub.app.data.entity.UserRole
import com.submithub.app.databinding.ActivityAdminDashboardBinding
import com.submithub.app.databinding.DialogAddUserBinding
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var repository: Repository
    private lateinit var codeAdapter: AdminCodeAdapter
    private lateinit var userAdapter: AdminUserAdapter

    override fun onResume() {
        super.onResume()
        com.submithub.app.util.SessionManager.updateLastActive(this)
    }

    private var showingCodes = true
    private var latestCodes: List<CodeEntity> = emptyList()
    private var latestUsers: List<UserEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = Repository()

        val adminName = intent.getStringExtra("adminName") ?: "Admin"
        binding.tvGreeting.text = "Welcome, $adminName"
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }


        codeAdapter = AdminCodeAdapter { code -> confirmDeleteCode(code) }
        binding.rvCodes.layoutManager = LinearLayoutManager(this)
        binding.rvCodes.adapter = codeAdapter

        userAdapter = AdminUserAdapter { user -> confirmDeleteUser(user) }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter

        binding.btnTabCodes.setOnClickListener { showCodesTab() }
        binding.btnTabUsers.setOnClickListener { showUsersTab() }

        binding.btnAddUser.setOnClickListener { showAddUserDialog() }
        binding.btnAddUser.setOnLongClickListener {
            lifecycleScope.launch {
                val results = repository.migrateOldAccountsToAuth()
                AlertDialog.Builder(this@AdminDashboardActivity)
                    .setTitle("Migration Results")
                    .setMessage(results.joinToString("\n").ifEmpty { "No old accounts found to migrate." })
                    .setPositiveButton("OK", null)
                    .show()
            }
            true
        }

        repository.allCodes().observe(this) { codes ->
            latestCodes = codes
            binding.tvCodeCount.text = codes.size.toString()
            if (showingCodes) {
                codeAdapter.submitList(codes)
                updateEmptyState()
            }
        }

        repository.allSubmissions().observe(this) { submissions ->
            binding.tvSubmissionCount.text = submissions.size.toString()
        }

        repository.allUsers().observe(this) { users ->
            latestUsers = users
            binding.tvLecturerCount.text = users.count { it.role == UserRole.LECTURER }.toString()
            binding.tvStudentCount.text = users.count { it.role == UserRole.STUDENT }.toString()
            if (!showingCodes) {
                userAdapter.submitList(users)
                updateEmptyState()
            }
        }

        showCodesTab()
    }

    private fun showCodesTab() {
        showingCodes = true
        binding.btnTabCodes.setBackgroundColor(getColor(com.submithub.app.R.color.primary))
        binding.btnTabUsers.setBackgroundColor(getColor(com.submithub.app.R.color.surface))
        binding.rvCodes.visibility = View.VISIBLE
        binding.rvUsers.visibility = View.GONE
        codeAdapter.submitList(latestCodes)
        updateEmptyState()
    }

    private fun showUsersTab() {
        showingCodes = false
        binding.btnTabUsers.setBackgroundColor(getColor(com.submithub.app.R.color.primary))
        binding.btnTabCodes.setBackgroundColor(getColor(com.submithub.app.R.color.surface))
        binding.rvCodes.visibility = View.GONE
        binding.rvUsers.visibility = View.VISIBLE
        userAdapter.submitList(latestUsers)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = if (showingCodes) latestCodes.isEmpty() else latestUsers.isEmpty()
        binding.tvEmpty.text = if (showingCodes) "No assignment codes yet." else "No users yet."
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun confirmDeleteCode(code: CodeEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete assignment code?")
            .setMessage("This will remove \"${code.assignmentTitle}\" (${code.id}) and all of its submissions. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteCode(code.id)
                    Toast.makeText(this@AdminDashboardActivity, "Code deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteUser(user: UserEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete user?")
            .setMessage("This will remove ${user.name} (${user.id}). This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteUser(user.id)
                    Toast.makeText(this@AdminDashboardActivity, "User deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddUserDialog() {
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)
        var selectedRole = UserRole.LECTURER

        fun refreshRoleButtons() {
            dialogBinding.btnRoleLecturer.setBackgroundColor(
                getColor(if (selectedRole == UserRole.LECTURER) com.submithub.app.R.color.primary else com.submithub.app.R.color.surface)
            )
            dialogBinding.btnRoleStudent.setBackgroundColor(
                getColor(if (selectedRole == UserRole.STUDENT) com.submithub.app.R.color.primary else com.submithub.app.R.color.surface)
            )
        }

        dialogBinding.btnRoleLecturer.setOnClickListener {
            selectedRole = UserRole.LECTURER
            refreshRoleButtons()
        }
        dialogBinding.btnRoleStudent.setOnClickListener {
            selectedRole = UserRole.STUDENT
            refreshRoleButtons()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etName.text.toString().trim()
            val id = dialogBinding.etId.text.toString().trim()
            val identifierLabel = dialogBinding.etIdentifierLabel.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString().trim()

            if (name.isEmpty() || id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in name, ID, and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                repository.createUser(
                    UserEntity(
                        id = id,
                        name = name,
                        role = selectedRole,
                        password = password,
                        identifierLabel = identifierLabel.ifEmpty { selectedRole.name }
                    )
                )
                Toast.makeText(this@AdminDashboardActivity, "$name added as ${selectedRole.name.lowercase()}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
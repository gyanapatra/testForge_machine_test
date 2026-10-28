package com.testforge.auth.entity;

import com.testforge.auth.dto.UserRole;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_app_users_username", columnNames = "username"),
		@UniqueConstraint(name = "uk_app_users_email", columnNames = "email") })
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 50)
	private String username;
	@Column(nullable = false)
	private String email;
	@Column(nullable = false)
	private String passwordHash;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserRole role;

	protected AppUser() {
	}

	public AppUser(String username, String email, String passwordHash, UserRole role) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}
}

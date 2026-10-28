package com.testforge.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Product name is required")
	@Size(max = 100, message = "Product name cannot exceed 100 characters")
	@Column(nullable = false, length = 120)
	private String name;

	@Size(max = 1000, message = "Description cannot exceed 1000 characters")
	private String description;

	@NotNull(message = "Product price is required")
	@DecimalMin("0.0")
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Min(0)
	@Column(nullable = false)
	private int quantity;

	protected Product() {
	}

	public Product(String name, String description, BigDecimal price, int quantity) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void update(String name, String description, BigDecimal price, int quantity) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.quantity = quantity;
	}
}

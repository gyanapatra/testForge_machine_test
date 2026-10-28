import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import {
  Product,
  ProductRequest,
  ProductService
} from '../../services/product';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './products.html',
  styleUrl: './products.css'
})
export class Products implements OnInit {

  products: Product[] = [];
  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  search = '';
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  sortBy = 'name';
  direction: 'asc' | 'desc' = 'asc';

  showForm = false;
  editingId: number | null = null;

  form: ProductRequest = this.emptyForm();

constructor(
  private productService: ProductService,
  private cdr: ChangeDetectorRef
) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  private emptyForm(): ProductRequest {
    return {
      name: '',
      description: '',
      price: 0,
      quantity: 0
    };
  }

loadProducts(): void {
  this.loading = true;
  this.errorMessage = '';
  this.cdr.detectChanges();

  this.productService.getProducts(
    this.page,
    this.size,
    this.search.trim(),
    this.sortBy,
    this.direction
  ).subscribe({
    next: (response: any) => {
      console.log('Products API response:', response);

      this.products = response?.content ?? [];
      this.totalElements =
        response?.totalElements ?? this.products.length;
      this.totalPages = response?.totalPages ?? 0;
      this.page = response?.number ?? this.page;

      this.loading = false;

      // Explicitly update the rendered view.
      this.cdr.detectChanges();

      console.log('Products loaded:', this.products.length);
    },

    error: (error) => {
      console.error('Products API error:', error);

      this.loading = false;
      this.errorMessage =
        error.error?.message ||
        `Unable to load products. HTTP status: ${error.status}`;

      this.cdr.detectChanges();
    }
  });
}

  searchProducts(): void {
    this.page = 0;
    this.loadProducts();
  }

  changeSort(field: string): void {
    if (this.sortBy === field) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.direction = 'asc';
    }

    this.page = 0;
    this.loadProducts();
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadProducts();
    }
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadProducts();
    }
  }

  openCreateForm(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  openEditForm(product: Product): void {
    this.editingId = product.id;
    this.form = {
      name: product.name,
      description: product.description ?? '',
      price: product.price,
      quantity: product.quantity
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.form = this.emptyForm();
  }

  saveProduct(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.form.name.trim()) {
      this.errorMessage = 'Product name is required.';
      return;
    }

    if (
      !Number.isFinite(Number(this.form.price)) ||
      Number(this.form.price) < 0
    ) {
      this.errorMessage = 'Enter a valid, non-negative price.';
      return;
    }

    if (
      !Number.isInteger(Number(this.form.quantity)) ||
      Number(this.form.quantity) < 0
    ) {
      this.errorMessage = 'Quantity must be a non-negative whole number.';
      return;
    }

    const request: ProductRequest = {
      name: this.form.name.trim(),
      description: this.form.description.trim(),
      price: Number(this.form.price),
      quantity: Number(this.form.quantity)
    };

    const productId = this.editingId;
    this.saving = true;

    const operation = productId === null
      ? this.productService.createProduct(request)
      : this.productService.updateProduct(productId, request);

    operation.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;

        this.successMessage = productId === null
          ? 'Product created successfully.'
          : 'Product updated successfully.';

        this.editingId = null;
        this.form = this.emptyForm();
        this.loadProducts();
      },
      error: error => {
        this.saving = false;
        this.errorMessage =
          error.error?.message || 'Unable to save the product.';
        console.error('Save product failed:', error);
      }
    });
  }

  deleteProduct(product: Product): void {
    if (!window.confirm(`Delete "${product.name}"?`)) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.productService.deleteProduct(product.id).subscribe({
      next: () => {
        this.successMessage = 'Product deleted successfully.';

        if (this.products.length === 1 && this.page > 0) {
          this.page--;
        }

        this.loadProducts();
      },
      error: error => {
        this.errorMessage =
          error.error?.message || 'Unable to delete the product.';
        console.error('Delete product failed:', error);
      }
    });
  }
}
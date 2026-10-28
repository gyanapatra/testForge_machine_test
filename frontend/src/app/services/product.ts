import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
}

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  quantity: number;
}

export interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

getProducts(
  page = 0,
  size = 10,
  search = '',
  sortBy = 'name',
  direction = 'asc'
): Observable<ProductPage> {
  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('search', search)
    .set('sortBy', sortBy)
    .set('direction', direction);

  return this.http.get<ProductPage>(this.apiUrl, { params });
}

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(
    id: number,
    request: ProductRequest
  ): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
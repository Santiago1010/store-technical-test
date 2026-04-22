import { setActivePinia, createPinia } from 'pinia'
import { useProductsStore } from '../products.store'
import { productsApi } from '@/api/client'
import { vi, describe, it, expect, beforeEach } from 'vitest'

vi.mock('@/api/client', () => ({
  productsApi: { list: vi.fn(), create: vi.fn(), update: vi.fn(), delete: vi.fn() }
}))

describe('products.store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('fetchProducts — cache miss calls API', async () => {
    productsApi.list.mockResolvedValue({ data: [{ id: '1' }] })
    const store = useProductsStore()
    const result = await store.fetchProducts({})
    expect(productsApi.list).toHaveBeenCalledTimes(1)
    expect(result).toEqual([{ id: '1' }])
  })

  it('fetchProducts — cache hit skips API', async () => {
    productsApi.list.mockResolvedValue({ data: [] })
    const store = useProductsStore()
    await store.fetchProducts({ page: 1 })
    await store.fetchProducts({ page: 1 })
    expect(productsApi.list).toHaveBeenCalledTimes(1)
  })

  it('fetchProducts — error sets store.error', async () => {
    productsApi.list.mockRejectedValue({ detail: 'Server error' })
    const store = useProductsStore()
    await expect(store.fetchProducts({})).rejects.toBeDefined()
    expect(store.error).toBe('Server error')
  })

  it('createProduct invalidates cache', async () => {
    productsApi.list.mockResolvedValue({ data: [] })
    productsApi.create.mockResolvedValue({ data: { id: '2' } })
    const store = useProductsStore()
    await store.fetchProducts({})
    await store.createProduct({ sku: 'X', name: 'Y', price: 10 })
    expect(store.cache).toEqual({})
  })
})
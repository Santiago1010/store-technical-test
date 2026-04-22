import { setActivePinia, createPinia } from 'pinia'
import { useInventoryStore } from '../inventory.store'
import { inventoryApi, purchasesApi } from '@/api/client'
import { vi, describe, it, expect, beforeEach } from 'vitest'

vi.mock('@/api/client', () => ({
  inventoryApi: { getByProductId: vi.fn() },
  purchasesApi: { purchase: vi.fn() }
}))

describe('inventory.store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('fetchInventory — sets stock on success', async () => {
    inventoryApi.getByProductId.mockResolvedValue({ data: { available: 5 } })
    const store = useInventoryStore()
    await store.fetchInventory('pid-1')
    expect(store.stock).toEqual({ available: 5 })
    expect(store.loading).toBe(false)
  })

  it('fetchInventory — 503 maps to readable error', async () => {
    inventoryApi.getByProductId.mockRejectedValue({ status: 503 })
    const store = useInventoryStore()
    await expect(store.fetchInventory('pid-1')).rejects.toBeDefined()
    expect(store.error).toBe('Inventory service unavailable. Try again later.')
  })

  it('purchase — success sets success=true', async () => {
    purchasesApi.purchase.mockResolvedValue({})
    const store = useInventoryStore()
    await store.purchase('pid-1', 2)
    expect(store.success).toBe(true)
    expect(store.error).toBeNull()
  })

  it('purchase — 422 maps to insufficient stock message', async () => {
    purchasesApi.purchase.mockRejectedValue({ status: 422 })
    const store = useInventoryStore()
    await expect(store.purchase('pid-1', 99)).rejects.toBeDefined()
    expect(store.error).toBe('Insufficient stock')
  })

  it('purchase — 409 maps to conflict message', async () => {
    purchasesApi.purchase.mockRejectedValue({ status: 409 })
    const store = useInventoryStore()
    await expect(store.purchase('pid-1', 1)).rejects.toBeDefined()
    expect(store.error).toBe('Conflict (concurrent update)')
  })
})
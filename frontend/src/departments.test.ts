import { describe, expect, it } from 'vitest'
import { DEPARTMENTS, getDepartmentLabel } from './departments'

describe('departments', () => {
  it('matches the Department enum of the backend', () => {
    expect(DEPARTMENTS.map((dept) => dept.value)).toEqual([
      'RH',
      'FINANCE',
      'IT',
      'MARKETING',
      'DIRECTION',
      'SALES',
      'SUPPORT',
    ])
  })

  it('returns the label of a known department', () => {
    expect(getDepartmentLabel('RH')).toBe('Human Resources (HR)')
  })

  it('falls back to the raw value for an unknown department', () => {
    expect(getDepartmentLabel('LEGAL')).toBe('LEGAL')
  })
})

export const DEPARTMENTS = [
  { value: 'RH', label: 'Human Resources (HR)' },
  { value: 'FINANCE', label: 'Finance & Accounting' },
  { value: 'IT', label: 'IT & Engineering' },
  { value: 'MARKETING', label: 'Marketing & Com' },
  { value: 'DIRECTION', label: 'Management' },
  { value: 'SALES', label: 'Sales' },
  { value: 'SUPPORT', label: 'Customer Support' },
]

export const getDepartmentLabel = (value: string) =>
  DEPARTMENTS.find((dept) => dept.value === value)?.label ?? value

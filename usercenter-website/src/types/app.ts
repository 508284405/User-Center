/**
 * 应用变量定义
 */
export interface Variable {
  /** 变量名（唯一标识） */
  key: string
  /** 显示名称 */
  label: string
  /** 变量类型 */
  type: 'string' | 'number' | 'select' | 'textarea'
  /** 是否必填 */
  required: boolean
  /** 默认值 */
  defaultValue?: any
  /** 选项列表（当type为'select'时使用） */
  options?: Array<{ label: string; value: any }>
}

/**
 * 选项定义
 */
export interface VariableOption {
  /** 选项显示名称 */
  label: string
  /** 选项值 */
  value: any
}
// 代码文本管理器
export class CodeTextManager {
  constructor(options = {}) {
    this.config = {
      typeSpeed: 50, // 打字速度（毫秒）
      pauseDuration: 1500, // 每行显示完成后的暂停时间
      loopDelay: 2000, // 循环间隔
      ...options
    }
    
    this.currentIndex = 0
    this.isTyping = false
    this.isPaused = false
    this.typeInterval = null
    this.pauseTimeout = null
    this.onTextChange = null
    this.onTypingStateChange = null
  }
  
  // 设置回调函数
  setCallbacks(onTextChange, onTypingStateChange) {
    this.onTextChange = onTextChange
    this.onTypingStateChange = onTypingStateChange
  }
  
  // 打字机效果
  async typeText(text) {
    return new Promise((resolve) => {
      this.isTyping = true
      this.onTypingStateChange?.(true)
      
      let displayedText = ''
      let charIndex = 0
      
      this.typeInterval = setInterval(() => {
        if (charIndex < text.length) {
          displayedText += text[charIndex]
          this.onTextChange?.(displayedText)
          charIndex++
        } else {
          clearInterval(this.typeInterval)
          this.isTyping = false
          this.onTypingStateChange?.(false)
          
          // 暂停一段时间后继续
          this.pauseTimeout = setTimeout(() => {
            resolve()
          }, this.config.pauseDuration)
        }
      }, this.config.typeSpeed)
    })
  }
  
  // 开始循环显示文本
  async startLoop(textArray) {
    if (!textArray || textArray.length === 0) return
    
    while (!this.isPaused) {
      for (let i = 0; i < textArray.length; i++) {
        if (this.isPaused) break
        
        this.currentIndex = i
        await this.typeText(textArray[i])
        
        if (this.isPaused) break
      }
      
      // 循环间隔
      if (!this.isPaused) {
        await new Promise(resolve => {
          this.pauseTimeout = setTimeout(resolve, this.config.loopDelay)
        })
      }
    }
  }
  
  // 暂停动画
  pause() {
    this.isPaused = true
    if (this.typeInterval) {
      clearInterval(this.typeInterval)
      this.typeInterval = null
    }
    if (this.pauseTimeout) {
      clearTimeout(this.pauseTimeout)
      this.pauseTimeout = null
    }
    this.isTyping = false
    this.onTypingStateChange?.(false)
  }
  
  // 恢复动画
  resume() {
    this.isPaused = false
  }
  
  // 停止动画
  stop() {
    this.pause()
    this.currentIndex = 0
    this.onTextChange?.('')
  }
  
  // 更新配置
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig }
  }
  
  // 获取当前状态
  getState() {
    return {
      currentIndex: this.currentIndex,
      isTyping: this.isTyping,
      isPaused: this.isPaused
    }
  }
}

// 预定义的代码文本集合
export const CodeTextCollections = {
  login: [
    'const user = await authenticate(credentials);',
    'if (user.isValid) {',
    '  generateToken(user);',
    '  redirectTo("/dashboard");',
    '}',
    'export const verifySession = () => {',
    '  return jwt.verify(token, secret);',
    '};',
    'const loginSuccess = {',
    '  status: "authenticated",',
    '  timestamp: Date.now()',
    '};'
  ],
  register: [
    'const newUser = {',
    '  id: generateUUID(),',
    '  username: formData.username,',
    '  email: formData.email',
    '};',
    'await User.create(newUser);',
    'const welcome = "Account created!";',
    'sendWelcomeEmail(user.email);',
    'return {',
    '  success: true,',
    '  user: sanitizeUser(newUser)',
    '};'
  ],
  general: [
    'console.log("Hello, World!");',
    'const data = await fetchData();',
    'if (data.success) {',
    '  processData(data.result);',
    '}',
    'export default function App() {',
    '  return <div>Welcome</div>;',
    '}'
  ]
}

// 文本效果工具函数
export const TextEffects = {
  // 随机字符效果
  scrambleText(text, duration = 1000) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?'
    let result = text
    const originalText = text
    let iterations = 0
    const maxIterations = duration / 50
    
    return new Promise((resolve) => {
      const interval = setInterval(() => {
        result = originalText
          .split('')
          .map((char, index) => {
            if (index < iterations) {
              return originalText[index]
            }
            return chars[Math.floor(Math.random() * chars.length)]
          })
          .join('')
        
        iterations += 1/3
        
        if (iterations >= originalText.length) {
          clearInterval(interval)
          resolve(originalText)
        }
      }, 50)
    })
  },
  
  // 波浪文字效果
  waveText(text, callback) {
    const chars = text.split('')
    let currentIndex = 0
    
    const interval = setInterval(() => {
      const result = chars.map((char, index) => {
        if (index === currentIndex) {
          return `<span style="transform: translateY(-5px); display: inline-block;">${char}</span>`
        }
        return char
      }).join('')
      
      callback(result)
      currentIndex = (currentIndex + 1) % chars.length
    }, 200)
    
    return () => clearInterval(interval)
  },
  
  // 渐现文字效果
  fadeInText(text, callback, duration = 2000) {
    const chars = text.split('')
    const charDelay = duration / chars.length
    let visibleChars = 0
    
    const interval = setInterval(() => {
      const result = chars.map((char, index) => {
        if (index < visibleChars) {
          return `<span style="opacity: 1; transition: opacity 0.3s;">${char}</span>`
        }
        return `<span style="opacity: 0;">${char}</span>`
      }).join('')
      
      callback(result)
      visibleChars++
      
      if (visibleChars > chars.length) {
        clearInterval(interval)
      }
    }, charDelay)
    
    return () => clearInterval(interval)
  }
}

// 语法高亮工具
export const SyntaxHighlighter = {
  // 简单的 JavaScript 语法高亮
  highlightJS(code) {
    const keywords = ['const', 'let', 'var', 'function', 'if', 'else', 'for', 'while', 'return', 'export', 'import', 'await', 'async']
    const strings = /(['"`])((?:(?!\1)[^\\]|\\.)*)(\1)/g
    const comments = /(\/\/.*$|\/\*[\s\S]*?\*\/)/gm
    const numbers = /\b\d+(\.\d+)?\b/g
    
    let highlighted = code
    
    // 高亮字符串
    highlighted = highlighted.replace(strings, '<span style="color: #98D982;">$1$2$3</span>')
    
    // 高亮注释
    highlighted = highlighted.replace(comments, '<span style="color: #7C7C7C;">$1</span>')
    
    // 高亮数字
    highlighted = highlighted.replace(numbers, '<span style="color: #D19A66;">$&</span>')
    
    // 高亮关键字
    keywords.forEach(keyword => {
      const regex = new RegExp(`\\b${keyword}\\b`, 'g')
      highlighted = highlighted.replace(regex, `<span style="color: #C678DD;">${keyword}</span>`)
    })
    
    return highlighted
  },
  
  // 简单的 CSS 语法高亮
  highlightCSS(code) {
    const properties = /([a-zA-Z-]+)(\s*:\s*)/g
    const values = /:\s*([^;]+);/g
    const selectors = /^([^{]+){/gm
    
    let highlighted = code
    
    // 高亮选择器
    highlighted = highlighted.replace(selectors, '<span style="color: #E06C75;">$1</span>{')
    
    // 高亮属性
    highlighted = highlighted.replace(properties, '<span style="color: #56B6C2;">$1</span>$2')
    
    // 高亮值
    highlighted = highlighted.replace(values, ': <span style="color: #98D982;">$1</span>;')
    
    return highlighted
  }
} 
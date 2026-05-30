<template>
  <div class="app-container">
    <el-card class="material-container-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Notebook /></el-icon>
            <span>素材库管理 / Material Library</span>
          </div>
          <div class="header-ops">
            <el-button 
              v-if="activeTab === 'TEXT'"
              type="success" 
              :icon="Upload" 
              @click="openImportDialog" 
              style="margin-right: 8px"
            >
              批量导入文案
            </el-button>
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">
              新建素材
            </el-button>
          </div>
        </div>
      </template>

      <!-- Main Content Layout -->
      <div class="main-layout">
        <!-- Left Filters Bar -->
        <div class="filters-sidebar">

          <div class="filter-section">
            <div class="filter-title">
              <el-icon><Search /></el-icon>
              <span>名称搜索</span>
            </div>
            <el-input
              v-model="searchQuery"
              placeholder="搜索素材名称..."
              clearable
              :prefix-icon="Search"
              class="custom-input"
              @input="handleSearch"
            />
          </div>

          <div class="filter-section">
            <div class="filter-title">
              <el-icon><Filter /></el-icon>
              <span>来源分类 (小说/短剧)</span>
            </div>
            <el-radio-group v-model="sourceTypeFilter" class="custom-radio-group" @change="handleSearch">
              <el-radio-button label="">全部 / All</el-radio-button>
              <el-radio-button label="NOVEL">小说 / Novel</el-radio-button>
              <el-radio-button label="DRAMA">短剧 / Drama</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- Right Materials Display Area -->
        <div class="materials-area">
          <!-- Category Tabs -->
          <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="handleTabChange">
            <el-tab-pane label="全部素材" name="ALL" />
            <el-tab-pane label="标题文案" name="TEXT" />
            <el-tab-pane label="图片素材" name="IMAGE" />
            <el-tab-pane label="H5链接" name="H5" />
            <el-tab-pane label="小程序" name="MINIPROGRAM" />
          </el-tabs>

          <!-- Loading State -->
          <div v-if="loading" class="loading-container">
            <el-loading v-loading="true" element-loading-text="正在努力加载素材中..." />
          </div>

          <!-- Empty State -->
          <div v-else-if="materials.length === 0" class="empty-container">
            <el-empty description="暂无符合条件的素材，赶快新建一个吧！">
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">立即新建</el-button>
            </el-empty>
          </div>

          <!-- Materials Grid -->
          <div v-else class="materials-grid">
            <div v-for="item in materials" :key="item.id" class="material-card">
              <!-- Source Context Tag -->
              <div v-if="item.sourceType" class="source-badge" :class="item.sourceType.toLowerCase()">
                {{ item.sourceType === 'NOVEL' ? '小说' : '短剧' }}
              </div>

              <!-- Card Header (Type & Title) -->
              <div class="card-head">
                <div class="type-icon" :class="item.type.toLowerCase()">
                  <el-icon v-if="item.type === 'TEXT'"><Document /></el-icon>
                  <el-icon v-else-if="item.type === 'IMAGE'"><Picture /></el-icon>
                  <el-icon v-else-if="item.type === 'H5'"><Link /></el-icon>
                  <el-icon v-else-if="item.type === 'MINIPROGRAM'"><Compass /></el-icon>
                </div>
                <span class="type-text">{{ formatType(item.type) }}</span>
              </div>

              <h3 class="material-title" :title="item.title">{{ item.title }}</h3>

              <!-- Card Body customized by material type -->
              <div class="card-body">
                <!-- TEXT Type -->
                <div v-if="item.type === 'TEXT'" class="text-content">
                  {{ item.content }}
                </div>

                <!-- IMAGE Type -->
                <div v-else-if="item.type === 'IMAGE'" class="image-content">
                  <el-image
                    :src="item.content"
                    fit="cover"
                    class="preview-img"
                    :preview-src-list="[item.content]"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>图片已失效</span>
                      </div>
                    </template>
                  </el-image>
                </div>

                <!-- H5 Type -->
                <div v-else-if="item.type === 'H5'" class="h5-content">
                  <div class="link-label">推广链接:</div>
                  <div class="link-url" :title="item.content">{{ item.content }}</div>
                </div>

                <!-- MINIPROGRAM Type -->
                <div v-else-if="item.type === 'MINIPROGRAM'" class="mp-content">
                  <div class="mp-field">
                    <span class="label">AppID:</span>
                    <span class="value">{{ item.appId }}</span>
                  </div>
                  <div class="mp-field">
                    <span class="label">路径:</span>
                    <span class="value" :title="item.pagePath">{{ item.pagePath || '-' }}</span>
                  </div>
                </div>
              </div>

              <!-- Card Footer Quick actions -->
              <div class="card-footer">
                <div class="footer-time">{{ formatTime(item.updateTime || item.createTime) }}</div>
                <div class="action-buttons">
                  <!-- Copy Content button (H5 / Path / Text) -->
                  <el-tooltip :content="item.type === 'MINIPROGRAM' ? '复制素材' : '复制内容'" placement="top">
                    <el-button 
                      circle 
                      size="small" 
                      :icon="CopyDocument" 
                      class="btn-action copy"
                      @click="handleCopy(item)" 
                    />
                  </el-tooltip>
                  <el-tooltip content="编辑" placement="top">
                    <el-button 
                      circle 
                      size="small" 
                      :icon="Edit" 
                      class="btn-action edit"
                      @click="openEditDialog(item)" 
                    />
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button 
                      circle 
                      size="small" 
                      :icon="Delete" 
                      class="btn-action delete"
                      @click="handleDelete(item)" 
                    />
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination Block -->
          <div class="pagination-block" v-if="materials.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 48, 96]"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑素材 / Edit Material' : '新建素材 / New Material'"
      width="650px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px" class="custom-form">
        <!-- Material Type (Disabled on Edit to maintain integrity) -->
        <el-form-item label="素材类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="isEdit" class="custom-radio-group-horizontal large">
            <el-radio-button label="TEXT">标题文案</el-radio-button>
            <el-radio-button label="IMAGE">图片</el-radio-button>
            <el-radio-button label="H5">H5链接</el-radio-button>
            <el-radio-button label="MINIPROGRAM">小程序</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- Material Name -->
        <el-form-item label="素材名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入素材名称..." maxlength="20" show-word-limit />
        </el-form-item>

        <!-- Source classification (For H5 & MiniProgram) -->
        <el-form-item 
          v-if="form.type === 'H5' || form.type === 'MINIPROGRAM'" 
          label="来源分类" 
          prop="sourceType"
        >
          <el-select v-model="form.sourceType" placeholder="请选择内容归属属性..." style="width: 100%">
            <el-option label="小说 / Novel" value="NOVEL" />
            <el-option label="短剧 / Short Drama" value="DRAMA" />
          </el-select>
        </el-form-item>

        <!-- Conditional Form Fields by Type -->
        
        <!-- 1. TEXT Type Form -->
        <el-form-item v-if="form.type === 'TEXT'" label="文案内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入文案的核心文字内容，不能超过 50 字..."
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <!-- 2. IMAGE Type Form -->
        <el-form-item v-if="form.type === 'IMAGE'" label="图片来源">
          <el-radio-group v-model="imageSourceType" style="margin-bottom: 15px">
            <el-radio label="upload">本地上传 (持久化存储)</el-radio>
            <el-radio label="url">在线网络链接 URL</el-radio>
          </el-radio-group>

          <!-- Local Upload Uploader -->
          <div v-if="imageSourceType === 'upload'" class="uploader-wrapper">
            <el-upload
              class="premium-uploader"
              action=""
              :http-request="handleImageUpload"
              :show-file-list="false"
              v-loading="uploading"
            >
              <div v-if="form.content" class="preview-container">
                <img :src="form.content" class="upload-preview" />
                <div class="preview-overlay">
                  <el-icon class="overlay-icon"><Edit /></el-icon>
                  <span>更换图片 / Change</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Picture /></el-icon>
                <div class="upload-text">点击上传本地图片</div>
                <div class="upload-hint">支持 PNG, JPG, GIF 格式</div>
              </div>
            </el-upload>
          </div>

          <!-- URL input -->
          <el-input 
            v-else 
            v-model="form.content" 
            placeholder="请输入以 http:// 或 https:// 开头的网络图片链接..." 
          />
        </el-form-item>

        <!-- 3. H5 Type Form -->
        <el-form-item v-if="form.type === 'H5'" label="H5 链接 URL" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入小说的 H5 推广链接 URL，如 http://..." 
          />
        </el-form-item>

        <!-- 4. MINIPROGRAM Type Form -->
        <template v-if="form.type === 'MINIPROGRAM'">
          <el-form-item label="小程序 AppID" prop="appId">
            <el-input v-model="form.appId" placeholder="请输入小程序的 appId，如 wx..." />
          </el-form-item>
          <el-form-item label="小程序路径" prop="pagePath">
            <el-input v-model="form.pagePath" placeholder="请输入小程序的落地页路径 page path，如 pages/index/index..." />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">确定保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Bulk Import Dialog -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入文案 / Bulk Import Text"
      width="600px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form ref="importFormRef" :model="importForm" :rules="importFormRules" label-width="120px" class="custom-form">
        <el-form-item label="素材名称前缀" prop="baseTitle">
          <el-input 
            v-model="importForm.baseTitle" 
            placeholder="请输入素材的基础名称，将自动拼接 #序号..." 
            maxlength="15" 
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="导入文案文件">
          <el-upload
            class="upload-txt-area"
            drag
            action=""
            :auto-upload="false"
            :show-file-list="false"
            accept=".txt"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将 .txt 文件拖到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip text-warning">
                提示：支持 .txt 文本文件，每行作为一条独立的标题文案，使用换行符分隔。
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- Preview Area -->
        <el-form-item v-if="parsedLines.length > 0" label="解析预览">
          <div class="import-preview-wrapper">
            <div class="preview-summary">
              解析成功！共发现 <span class="text-success">{{ parsedLines.length }}</span> 条有效文案。
            </div>
            <el-scrollbar max-height="200px" class="preview-scroll">
              <div v-for="(line, idx) in parsedLines.slice(0, 10)" :key="idx" class="preview-line-item">
                <span class="p-name">{{ importForm.baseTitle ? importForm.baseTitle : '素材名称' }}#{{ idx + 1 }}</span>
                <span class="p-divider">:</span>
                <span class="p-content" :title="line">{{ line }}</span>
              </div>
              <div v-if="parsedLines.length > 10" class="preview-line-more">
                ... 还有 {{ parsedLines.length - 10 }} 条文案未显示
              </div>
            </el-scrollbar>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="importing" :disabled="parsedLines.length === 0" @click="submitImportForm">确定导入</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMaterials, saveMaterial, deleteMaterial, uploadMaterialLocal, saveMaterialBatch } from '@/api/material'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Filter, Document, Picture, Link, Compass, Edit, Delete, CopyDocument, Notebook, Upload, UploadFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const activeTab = ref('ALL')
const searchQuery = ref('')
const sourceTypeFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const materials = ref<any[]>([])

const imageSourceType = ref('upload') // upload or url

const formRef = ref()
const form = reactive({
  id: null as number | null,
  title: '',
  type: 'TEXT', // TEXT, IMAGE, H5, MINIPROGRAM
  content: '',
  appId: '',
  pagePath: '',
  sourceType: '' // NOVEL, DRAMA
})

const validateContent = (_rule: any, value: any, callback: any) => {
  if (form.type === 'TEXT') {
    if (!value || value.trim() === '') {
      return callback(new Error('请输入文案内容'))
    }
    if (value.length > 50) {
      return callback(new Error('文案内容不能超过 50 个字'))
    }
  } else if (form.type === 'H5') {
    if (!value || value.trim() === '') {
      return callback(new Error('请输入 H5 推广链接'))
    }
    if (!value.startsWith('http')) {
      return callback(new Error('H5 推广链接必须以 http:// 或 https:// 开头'))
    }
  } else if (form.type === 'IMAGE') {
    if (!value || value.trim() === '') {
      return callback(new Error('请上传本地图片或输入在线图片 URL'))
    }
  }
  callback()
}

const formRules = {
  title: [
    { required: true, message: '请输入素材名称', trigger: 'blur' },
    { required: true, message: '请输入素材名称', trigger: 'change' },
    { max: 20, message: '素材名称不能超过 20 个字', trigger: 'blur' },
    { max: 20, message: '素材名称不能超过 20 个字', trigger: 'change' }
  ],
  content: [
    { validator: validateContent, trigger: 'blur' },
    { validator: validateContent, trigger: 'change' }
  ],
  appId: [{ required: true, message: '请输入小程序 AppID', trigger: 'blur' }],
  pagePath: [{ required: true, message: '请输入小程序页面路径', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源分类属性', trigger: 'change' }]
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMaterials({
      type: activeTab.value === 'ALL' ? undefined : activeTab.value,
      title: searchQuery.value || undefined,
      sourceType: sourceTypeFilter.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    }) as any
    if (res && res.content) {
      materials.value = res.content
      total.value = res.totalElements
    } else {
      materials.value = res || []
      total.value = materials.value.length
    }
  } catch (e) {
    ElMessage.error('加载素材列表失败，请重试。')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const formatType = (type: string) => {
  switch (type) {
    case 'TEXT': return '标题文案'
    case 'IMAGE': return '图片'
    case 'H5': return 'H5链接'
    case 'MINIPROGRAM': return '小程序'
    default: return type
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

const handleCopy = (item: any) => {
  if (item.type === 'MINIPROGRAM') {
    isEdit.value = false
    let copyTitle = item.title + ' - 副本'
    if (copyTitle.length > 20) {
      copyTitle = copyTitle.substring(0, 20)
    }
    Object.assign(form, {
      id: null,
      title: copyTitle,
      type: 'MINIPROGRAM',
      content: '',
      appId: item.appId,
      pagePath: item.pagePath,
      sourceType: item.sourceType
    })
    dialogVisible.value = true
    ElMessage.info('已复制小程序配置，可修改名称后点击保存')
  } else {
    const textToCopy = item.content
    navigator.clipboard.writeText(textToCopy).then(() => {
      ElMessage.success('成功复制到剪贴板！')
    }).catch(() => {
      ElMessage.error('复制失败，请手动选择复制。')
    })
  }
}

// Fixed openCreateDialog to dynamically set type based on the activeTab
const openCreateDialog = () => {
  isEdit.value = false
  const defaultType = activeTab.value === 'ALL' ? 'TEXT' : activeTab.value
  Object.assign(form, {
    id: null,
    title: '',
    type: defaultType,
    content: '',
    appId: '',
    pagePath: '',
    sourceType: ''
  })
  imageSourceType.value = 'upload'
  dialogVisible.value = true
}

const openEditDialog = (item: any) => {
  isEdit.value = true
  Object.assign(form, item)
  if (item.type === 'IMAGE') {
    imageSourceType.value = item.content.startsWith('/api/materials/local') ? 'upload' : 'url'
  }
  dialogVisible.value = true
}

const handleImageUpload = async (options: any) => {
  uploading.value = true
  try {
    const res = await uploadMaterialLocal(options.file) as any
    form.content = res.url
    ElMessage.success('图片上传成功！')
  } catch (e) {
    ElMessage.error('图片上传失败，请重试。')
  } finally {
    uploading.value = false
  }
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm(`确定要彻底删除素材 “${item.title}” 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    buttonSize: 'default'
  }).then(async () => {
    try {
      await deleteMaterial(item.id)
      ElMessage.success('素材已成功删除！')
      fetchData()
    } catch (e) {
      ElMessage.error('素材删除失败。')
    }
  })
}

const formatMiniProgramPath = (path: string): string => {
  if (!path) return ''
  const trimmed = path.trim()
  if (trimmed === '') return ''

  const qIndex = trimmed.indexOf('?')
  if (qIndex > -1) {
    const mainPath = trimmed.substring(0, qIndex)
    const query = trimmed.substring(qIndex + 1)
    if (!mainPath.endsWith('.html')) {
      return `${mainPath}.html?${query}`
    }
  } else {
    if (!trimmed.endsWith('.html')) {
      return `${trimmed}.html`
    }
  }
  return trimmed
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return

    if (form.type === 'TEXT' && form.content.length > 50) {
      return ElMessage.warning('文案内容不能超过 50 个字！')
    }

    if (form.type === 'H5' && !form.content.startsWith('http')) {
      return ElMessage.warning('H5 链接必须以 http:// 或 https:// 开头！')
    }

    if (form.type === 'IMAGE' && !form.content) {
      return ElMessage.warning('图片素材内容不能为空，请上传本地图片或输入在线 URL！')
    }

    if (form.type === 'MINIPROGRAM' && form.pagePath) {
      form.pagePath = formatMiniProgramPath(form.pagePath)
    }

    saving.value = true
    try {
      await saveMaterial(form)
      ElMessage.success('素材保存成功！')
      dialogVisible.value = false
      fetchData()
    } catch (e) {
      ElMessage.error('素材保存失败，请检查网络或表单内容。')
    } finally {
      saving.value = false
    }
  })
}

// Bulk Import Logic
const importDialogVisible = ref(false)
const importing = ref(false)
const parsedLines = ref<string[]>([])
const importFormRef = ref()
const importForm = reactive({
  baseTitle: ''
})

const importFormRules = {
  baseTitle: [
    { required: true, message: '请输入素材名称前缀', trigger: 'blur' },
    { max: 15, message: '前缀不能超过 15 个字，以确保序号有足够空间', trigger: 'blur' },
    { max: 15, message: '前缀不能超过 15 个字，以确保序号有足够空间', trigger: 'change' }
  ]
}

const openImportDialog = () => {
  importForm.baseTitle = ''
  parsedLines.value = []
  importDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  const rawFile = file.raw
  if (!rawFile) return
  
  if (!rawFile.name.endsWith('.txt')) {
    ElMessage.error('仅支持上传 .txt 格式的文案文件！')
    return
  }

  const reader = new FileReader()
  reader.onload = (e: any) => {
    const text = e.target.result || ''
    // Split by newlines, trim lines and filter out empty ones
    const lines = text
      .split(/\r?\n/)
      .map((line: string) => line.trim())
      .filter((line: string) => line.length > 0)
      
    if (lines.length === 0) {
      ElMessage.warning('上传的文件内未发现有效文案内容，请核对后再上传！')
      parsedLines.value = []
      return
    }

    // Dynamic line length limit validation (<= 50 chars)
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].length > 50) {
        ElMessage.error(`导入失败：第 ${i + 1} 行文案内容超出 50 字上限限制（当前 ${lines[i].length} 字），请修改后重新上传！`)
        parsedLines.value = []
        return
      }
    }

    parsedLines.value = lines
    ElMessage.success(`成功解析 ${lines.length} 条文案！`)
  }
  reader.readAsText(rawFile, 'UTF-8')
}

const submitImportForm = () => {
  importFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    
    if (parsedLines.value.length === 0) {
      return ElMessage.warning('请先选择有效的 .txt 文本文件上传！')
    }

    const baseName = importForm.baseTitle.trim()
    
    // Validation: Ensure prefix + "#" + max_index does not exceed 20 characters
    const maxIndexString = `#${parsedLines.value.length}`
    const fullMaxTitle = baseName + maxIndexString
    if (fullMaxTitle.length > 20) {
      return ElMessage.error(`素材名称拼接序号后（如“${fullMaxTitle}”）将达到 ${fullMaxTitle.length} 字，超出 20 字上限，请缩短名称前缀！`)
    }

    importing.value = true
    try {
      const batchData = parsedLines.value.map((line, idx) => ({
        title: `${baseName}#${idx + 1}`,
        type: 'TEXT',
        content: line
      }))

      await saveMaterialBatch(batchData)
      ElMessage.success(`批量导入成功，已成功创建 ${batchData.length} 个文案素材！`)
      importDialogVisible.value = false
      fetchData()
    } catch (e: any) {
      ElMessage.error('批量导入失败，请检查网络或格式是否正确。')
    } finally {
      importing.value = false
    }
  })
}
</script>

<style scoped>
.app-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.material-container-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

/* Premium White Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

/* Main Two-column layout */
.main-layout {
  display: flex;
  gap: 24px;
}

/* Left filter sidebar */
.filters-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: fit-content;
}

.sidebar-divider {
  margin: 10px 0;
}

.btn-sidebar-create {
  width: 100%;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.btn-sidebar-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.filter-title .el-icon {
  color: #409eff;
  font-size: 15px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 2px 10px;
}

.custom-radio-group {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.custom-radio-group :deep(.el-radio-button) {
  width: 100%;
}

.custom-radio-group :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 6px !important;
  border: 1px solid #dcdfe6 !important;
  margin-bottom: 6px;
  text-align: left;
  padding: 8px 12px;
  box-shadow: none !important;
  font-size: 13px;
  transition: all 0.2s ease;
}

.custom-radio-group :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background-color: #ecf5ff !important;
  color: #409eff !important;
  border-color: #409eff !important;
  font-weight: 600;
}

/* Right display area */
.materials-area {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0; /* Prevent flex blowouts */
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 0 20px;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
}

/* Loading & Empty Containers */
.loading-container, .empty-container {
  background: #fff;
  border-radius: 12px;
  padding: 80px 0;
  border: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Materials Responsive Grid */
.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* Premium Material Card styling */
.material-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  border: 1px solid #ebeef5;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 220px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

.material-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border-color: #dcdfe6;
}

/* Source Type Context Badges */
.source-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}

.source-badge.novel {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #faecd8;
}

.source-badge.drama {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

/* Card Type Header */
.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
}

.type-icon.text { background: linear-gradient(135deg, #409eff, #53a8ff); }
.type-icon.image { background: linear-gradient(135deg, #e6a23c, #f3d19e); }
.type-icon.h5 { background: linear-gradient(135deg, #67c23a, #85ce61); }
.type-icon.miniprogram { background: linear-gradient(135deg, #b37feb, #d3adf7); }

.type-text {
  font-size: 11px;
  font-weight: 600;
  color: #909399;
}

.material-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 10px 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-right: 40px;
}

/* Customized bodies */
.card-body {
  flex-grow: 1;
  min-height: 80px;
  max-height: 80px;
  overflow: hidden;
}

.text-content {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-content {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  background: #f4f6f8;
}

.preview-img {
  width: 100%;
  height: 100%;
  transition: all 0.3s;
}

.preview-img:hover {
  transform: scale(1.05);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  font-size: 11px;
  gap: 4px;
}

.image-error .el-icon {
  font-size: 18px;
}

.h5-content {
  background: #f8fafd;
  border: 1px solid #ecf3fc;
  border-radius: 6px;
  padding: 8px 10px;
  height: 85%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.link-label {
  font-size: 10px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 2px;
}

.link-url {
  font-size: 11px;
  color: #606266;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.3;
}

.mp-content {
  background: #fbf8fe;
  border: 1px solid #f6f0fd;
  border-radius: 6px;
  padding: 8px 10px;
  height: 85%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.mp-field {
  display: flex;
  font-size: 11px;
  min-width: 0;
}

.mp-field .label {
  color: #909399;
  font-weight: 500;
  width: 45px;
  flex-shrink: 0;
}

.mp-field .value {
  color: #606266;
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

/* Card footer and actions */
.card-footer {
  border-top: 1px solid #f2f6fc;
  padding-top: 10px;
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-time {
  font-size: 10px;
  color: #c0c4cc;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.btn-action {
  border: none;
  background: #f4f6f8;
  color: #606266;
  transition: all 0.2s;
}

.btn-action:hover.copy {
  background: #e1f3d8;
  color: #67c23a;
}

.btn-action:hover.edit {
  background: #ecf5ff;
  color: #409eff;
}

.btn-action:hover.delete {
  background: #fef0f0;
  color: #f56c6c;
}

/* Pagination bar */
.pagination-block {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* Premium Dialog Form */
.custom-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f2f6fc;
  padding-bottom: 16px;
  margin-right: 0;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #303133;
}

.custom-form {
  padding-top: 10px;
}

.custom-radio-group-horizontal.large :deep(.el-radio-button__inner) {
  padding: 10px 18px;
  font-weight: 600;
}

.uploader-wrapper {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.premium-uploader :deep(.el-upload) {
  border: 1px dashed #dcdfe6;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 240px;
  height: 140px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: #fafafa;
  box-shadow: 0 1px 4px rgba(0,0,0,0.01);
}

.premium-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
  border-style: solid;
  background: #f5f8ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  color: #909399;
  padding: 16px;
  box-sizing: border-box;
}

.upload-placeholder .upload-icon {
  font-size: 28px;
  color: #409eff;
  margin-bottom: 8px;
  opacity: 0.8;
  transition: all 0.3s;
}

.premium-uploader :deep(.el-upload:hover) .upload-icon {
  transform: scale(1.1);
  opacity: 1;
}

.upload-text {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.upload-hint {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* Preview with Hover Overlay */
.preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  opacity: 0;
  transition: all 0.3s ease;
}

.preview-container:hover .preview-overlay {
  opacity: 1;
}

.overlay-icon {
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f2f6fc;
  padding-top: 16px;
}

/* Bulk Import Styling */
.upload-txt-area :deep(.el-upload-dragger) {
  padding: 30px 10px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
}

.upload-txt-area :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

.text-warning {
  color: #e6a23c;
  font-size: 12px;
  margin-top: 6px;
  line-height: 1.4;
}

.import-preview-wrapper {
  background: #f8f9fa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  width: 100%;
}

.preview-summary {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.preview-scroll {
  border: 1px solid #e4e7ed;
  background: #fff;
  border-radius: 4px;
  padding: 8px;
}

.preview-line-item {
  display: flex;
  font-size: 12px;
  line-height: 1.8;
  border-bottom: 1px dashed #f2f6fc;
}

.preview-line-item:last-child {
  border-bottom: none;
}

.preview-line-item .p-name {
  color: #409eff;
  font-weight: 600;
  flex-shrink: 0;
}

.preview-line-item .p-divider {
  margin: 0 6px;
  color: #c0c4cc;
}

.preview-line-item .p-content {
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
}

.preview-line-more {
  font-size: 11px;
  color: #909399;
  text-align: center;
  margin-top: 6px;
  font-style: italic;
}
</style>

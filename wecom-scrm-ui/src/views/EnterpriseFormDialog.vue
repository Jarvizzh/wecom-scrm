<template>
  <el-dialog 
    v-model="visible" 
    :title="isEdit ? '编辑企业信息' : '添加新企业 (将会自动创建数据库)'" 
    :width="isMobile ? '95%' : '600px'"
    destroy-on-close
    append-to-body
  >
    <el-form :model="form" :label-width="isMobile ? 'auto' : '120px'" :label-position="isMobile ? 'top' : 'left'" :rules="rules" ref="formRef">
      <el-form-item label="企业名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入系统展示名称" />
      </el-form-item>
      <el-form-item label="企业ID" prop="corpId">
        <el-input v-model="form.corpId" placeholder="企业微信CorpId" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="AgentId" prop="agentId">
        <el-input v-model="form.agentId" placeholder="应用AgentId" type="number" />
      </el-form-item>
      <el-form-item label="AgentSecret" prop="agentSecret">
        <el-input v-model="form.agentSecret" :placeholder="isEdit ? '已加密隐藏 (如需修改请输入新值)' : '请输入应用AgentSecret'" show-password />
      </el-form-item>
      
      <el-divider>API回调配置</el-divider>
      <el-form-item label="Token" prop="token">
        <el-input v-model="form.token" :placeholder="isEdit ? '已加密隐藏 (如需修改请输入新值)' : '请输入Token'" />
      </el-form-item>
      <el-form-item label="EncodingAESKey" prop="encodingAesKey">
        <el-input v-model="form.encodingAesKey" :placeholder="isEdit ? '已加密隐藏 (如需修改请输入新值)' : '请输入EncodingAESKey'" />
      </el-form-item>
      
      <el-divider>数据库配置</el-divider>
      <el-form-item label="数据库URL" prop="dbUrl">
        <el-input v-model="form.dbUrl" placeholder="jdbc:mysql://127.0.0.1:3306/wecom_scrm_tenant" :disabled="isEdit" />
        <div style="font-size: 12px; color: #999;" v-if="!isEdit">会自动追加参数创建数据库</div>
      </el-form-item>
      <el-form-item label="数据库账号" prop="dbUsername">
        <el-input v-model="form.dbUsername" placeholder="root" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="数据库密码" prop="dbPassword">
        <el-input v-model="form.dbPassword" :placeholder="isEdit ? '已加密隐藏 (不可修改)' : '请输入数据库密码'" show-password :disabled="isEdit" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">{{ isEdit ? '保存修改' : '提交并初始化' }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useResponsive } from '@/hooks/useResponsive'

const { isMobile } = useResponsive()

const emit = defineEmits(['success'])
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  corpId: '',
  agentId: '',
  agentSecret: '',
  token: '',
  encodingAesKey: '',
  dbUrl: 'jdbc:mysql://127.0.0.1:3306/wecom_scrm_',
  dbUsername: 'root',
  dbPassword: '123456'
})

const rules = computed(() => ({
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  corpId: [{ required: true, message: '请输入CorpId', trigger: 'blur' }],
  agentId: [{ required: true, message: '请输入AgentId', trigger: 'blur' }],
  agentSecret: [{ required: !isEdit.value, message: '请输入AgentSecret', trigger: 'blur' }],
  dbUrl: [{ required: true, message: '请输入数据库URL', trigger: 'blur' }],
  dbUsername: [{ required: true, message: '请输入数据库账号', trigger: 'blur' }]
}))

const open = (data?: any) => {
  visible.value = true
  isEdit.value = !!data
  if (data) {
    currentId.value = data.id
    Object.assign(form, data)
  } else {
    currentId.value = null
    // Reset form when opening for add
    Object.assign(form, {
      name: '',
      corpId: '',
      agentId: '',
      agentSecret: '',
      token: '',
      encodingAesKey: '',
      dbUrl: 'jdbc:mysql://127.0.0.1:3306/wecom_scrm_',
      dbUsername: 'root',
      dbPassword: '123456'
    })
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value && currentId.value) {
          await request.put(`/enterprises/${currentId.value}`, form)
          ElMessage.success('企业信息修改成功！')
        } else {
          await request.post('/enterprises', form)
          ElMessage.success('企业添加成功，数据库已初始化！')
        }
        visible.value = false
        emit('success')
        window.dispatchEvent(new CustomEvent('refresh-enterprises'))
      } catch (error) {
        ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

defineExpose({
  open
})
</script>

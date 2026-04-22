<template>
  <div class="dashboard-container">
    <!-- Summary Section -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="item in summaryCards" :key="item.title" class="mb-20">
        <el-card shadow="hover" class="stats-card" :body-style="{ padding: '20px' }">
          <div class="card-content">
            <div class="card-info">
              <div class="card-title">{{ item.title }}</div>
              <div class="card-value">{{ item.value }}</div>
            </div>
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
          </div>
          <div class="card-footer" v-if="item.footer">
            <span class="footer-label">{{ item.footer }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Section -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近7日新增客户趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>客户来源分布</span>
            </div>
          </template>
          <div ref="sourceChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Alerts / Info Section -->
    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <div class="notice-list">
            <div class="notice-item">
              <span class="notice-tag">New</span>
              <span class="notice-text">WeCom SCRM 首页看板功能现已上线，欢迎体验！</span>
              <span class="notice-time">2026-04-09</span>
            </div>
            <div class="notice-item">
              <span class="notice-tag" style="background-color: #e6f7ff; color: #1890ff;">Info</span>
              <span class="notice-text">数据每日定时同步，手动同步可前往客户管理页面。</span>
              <span class="notice-time">2026-04-08</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/customers')">客户管理</el-button>
            <el-button type="success" @click="router.push('/group-chats')">群聊管理</el-button>
            <el-button type="warning" @click="router.push('/sync-logs')">同步记录</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { getStats } from '@/api/dashboard';
import { User, ChatDotRound, Collection, Connection } from '@element-plus/icons-vue';

const router = useRouter();

const stats = ref({
  totalCustomerCount: 0,
  todayNewCustomerCount: 0,
  totalEmployeeCount: 0,
  totalGroupChatCount: 0,
  totalMessageCount: 0,
  customerGrowthTrend: [] as any[],
  addWayDistribution: [] as any[]
});

const summaryCards = computed(() => [
  { 
    title: '总客户数', 
    value: stats.value.totalCustomerCount, 
    icon: User, 
    color: '#409EFF',
    footer: '累计服务客户数（去重）'
  },
  { 
    title: '今日新增', 
    value: stats.value.todayNewCustomerCount, 
    icon: Connection, 
    color: '#67C23A',
    footer: '当日加粉人数'
  },
  { 
    title: '员工总数', 
    value: stats.value.totalEmployeeCount, 
    icon: Collection, 
    color: '#E6A23C',
    footer: '启用服务员工'
  },
  { 
    title: '群聊总数', 
    value: stats.value.totalGroupChatCount, 
    icon: ChatDotRound, 
    color: '#F56C6C',
    footer: '已同步外部群'
  }
]);

const trendChartRef = ref<HTMLElement | null>(null);
const sourceChartRef = ref<HTMLElement | null>(null);
let trendChart: echarts.ECharts | null = null;
let sourceChart: echarts.ECharts | null = null;

const fetchStats = async () => {
  try {
    const res = await getStats() as any;
    stats.value = res;
    renderCharts();
  } catch (error) {
    console.error('Failed to fetch dashboard stats', error);
  }
};

const renderCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption({
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        boxShadow: '0 0 10px rgba(0,0,0,0.1)'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.customerGrowthTrend.map(i => i.date),
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed' } },
        axisLabel: { color: '#909399' }
      },
      series: [{
        name: '新增人数',
        data: stats.value.customerGrowthTrend.map(i => i.count),
        type: 'line',
        smooth: true,
        showSymbol: false,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0)' }
          ])
        },
        lineStyle: { width: 3, color: '#409EFF' },
        itemStyle: { color: '#409EFF' }
      }]
    });
  }

  if (sourceChartRef.value) {
    sourceChart = echarts.init(sourceChartRef.value);
    sourceChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', left: 'center', icon: 'circle', itemWidth: 8, itemHeight: 8 },
      series: [{
        name: '客户来源',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: stats.value.addWayDistribution
      }]
    });
  }
};

onMounted(() => {
  fetchStats();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

const handleResize = () => {
  trendChart?.resize();
  sourceChart?.resize();
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  .mb-20 {
    margin-bottom: 20px;
  }
}

.stats-card {
  border: none;
  border-radius: 12px;
  transition: all 0.3s;
}
.stats-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 24px;
}

.card-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f2f6fc;
  font-size: 12px;
}

.footer-label {
  color: #909399;
}

.charts-row {
  margin-top: 20px;
}

@media (max-width: 992px) {
  .charts-row .el-col + .el-col {
    margin-top: 20px;
  }
}

.chart-box {
  height: 350px;
}

.info-row {
  margin-top: 20px;
}

@media (max-width: 992px) {
  .info-row .el-col + .el-col {
    margin-top: 20px;
  }
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.notice-tag {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.notice-text {
  flex: 1;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  color: #909399;
  font-size: 12px;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
}
</style>

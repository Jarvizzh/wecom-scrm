<template>
  <div class="dashboard-container">
    <!-- Recharge Statistics Module (Super Admin Only) -->
    <div v-if="isSuperAdmin" class="dashboard-module recharge-module">
      <div class="module-header">
        <div class="header-left">
          <el-icon><Money /></el-icon>
          <span class="module-title">充值统计</span>
        </div>
        <div class="segmented-control">
          <div class="slider-bg" :class="rechargePeriodTab"></div>
          <div 
            class="segment-item" 
            :class="{ active: rechargePeriodTab === 'daily' }" 
            @click="setRechargePeriodTab('daily')"
          >
            按天统计
          </div>
          <div 
            class="segment-item" 
            :class="{ active: rechargePeriodTab === 'monthly' }" 
            @click="setRechargePeriodTab('monthly')"
          >
            按月统计
          </div>
        </div>
      </div>
      
      <!-- Recharge Summary Cards -->
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="item in rechargeCards" :key="item.title" class="mb-20">
          <el-card shadow="hover" class="stats-card recharge-card" :body-style="{ padding: '20px' }">
            <div class="card-content">
              <div class="card-info">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-value">¥ {{ item.value.toFixed(2) }}</div>
              </div>
              <div class="card-icon" :style="{ backgroundColor: item.color }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Recharge Trend & Products (Segmented Control Content) -->
      <div class="tab-content-wrapper">
        <Transition name="slide-fade">
          <div v-show="rechargePeriodTab === 'daily'" class="transition-container">
            <el-row :gutter="20" class="charts-row">
              <el-col :span="24" class="mb-30">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>近10日充值趋势</span>
                    </div>
                  </template>
                  <div ref="rechargeTrendChartRef" class="chart-box"></div>
                </el-card>
              </el-col>
              <el-col :span="24" class="mb-30">
                <el-card shadow="hover" class="product-stats-card">
                  <template #header>
                    <div class="card-header">
                      <span>分产品充值统计</span>
                    </div>
                  </template>
                  <el-tabs v-model="activeProductTab" class="custom-tabs">
                    <el-tab-pane label="阅文" name="yuewen">
                      <el-table :data="stats.yuewenRecharge?.productStats || []" size="small" height="350" stripe class="custom-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', borderBottom: '1px solid #ebeef5' }">
                        <el-table-column prop="productName" label="产品名称" min-width="100" fixed show-overflow-tooltip />
                        <el-table-column v-for="date in last10Days" :key="date" :label="formatDate(date)" min-width="110" align="right">
                          <template #default="{ row }">
                            <div class="amount-daily">¥ {{ getDailyAmount(row, date).toFixed(2) }}</div>
                            <div class="user-count">{{ getDailyUserCount(row, date) }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="今日" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-today">¥ {{ row.todayAmount.toFixed(2) }}</div>
                            <div class="user-count">{{ row.todayUserCount }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="本月" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-month">¥ {{ row.monthAmount.toFixed(2) }}</div>
                            <div class="user-count">{{ row.monthUserCount }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="上月" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-month" style="color: #E6A23C;">¥ {{ (row.lastMonthAmount || 0).toFixed(2) }}</div>
                            <div class="user-count">{{ row.lastMonthUserCount || 0 }} 人</div>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-tab-pane>
                    <el-tab-pane label="常读" name="changdu">
                      <el-table :data="stats.changduRecharge?.productStats || []" size="small" height="350" stripe class="custom-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', borderBottom: '1px solid #ebeef5' }">
                        <el-table-column prop="productName" label="产品名称" min-width="100" fixed show-overflow-tooltip />
                        <el-table-column v-for="date in last10Days" :key="date" :label="formatDate(date)" min-width="110" align="right">
                          <template #default="{ row }">
                            <div class="amount-daily">¥ {{ getDailyAmount(row, date).toFixed(2) }}</div>
                            <div class="user-count">{{ getDailyUserCount(row, date) }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="今日" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-today">¥ {{ row.todayAmount.toFixed(2) }}</div>
                            <div class="user-count">{{ row.todayUserCount }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="本月" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-month">¥ {{ row.monthAmount.toFixed(2) }}</div>
                            <div class="user-count">{{ row.monthUserCount }} 人</div>
                          </template>
                        </el-table-column>
                        <el-table-column label="上月" width="110" align="right" fixed="right">
                          <template #default="{ row }">
                            <div class="amount-month" style="color: #E6A23C;">¥ {{ (row.lastMonthAmount || 0).toFixed(2) }}</div>
                            <div class="user-count">{{ row.lastMonthUserCount || 0 }} 人</div>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-tab-pane>
                  </el-tabs>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </Transition>
        <Transition name="slide-fade">
          <div v-show="rechargePeriodTab === 'monthly'" class="transition-container">
            <el-row :gutter="20" class="charts-row">
              <el-col :span="24" class="mb-30">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>近半年充值趋势</span>
                    </div>
                  </template>
                  <div ref="rechargeHalfYearTrendChartRef" class="chart-box"></div>
                </el-card>
              </el-col>
              <el-col :span="24" class="mb-30">
                <el-card shadow="hover" class="product-stats-card">
                  <template #header>
                    <div class="card-header">
                      <span>分产品充值统计</span>
                    </div>
                  </template>
                  <el-tabs v-model="activeHalfYearProductTab" class="custom-tabs">
                    <el-tab-pane label="阅文" name="yuewen">
                      <el-table :data="stats.yuewenHalfYearRecharge?.productStats || []" size="small" height="350" stripe class="custom-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', borderBottom: '1px solid #ebeef5' }">
                        <el-table-column prop="productName" label="产品名称" min-width="100" fixed show-overflow-tooltip />
                        <el-table-column v-for="month in last6Months" :key="month" :label="formatMonth(month)" min-width="110" align="right">
                          <template #default="{ row }">
                            <div class="amount-daily">¥ {{ getMonthlyAmount(row, month).toFixed(2) }}</div>
                            <div class="user-count">{{ getMonthlyUserCount(row, month) }} 人</div>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-tab-pane>
                    <el-tab-pane label="常读" name="changdu">
                      <el-table :data="stats.changduHalfYearRecharge?.productStats || []" size="small" height="350" stripe class="custom-table" :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', borderBottom: '1px solid #ebeef5' }">
                        <el-table-column prop="productName" label="产品名称" min-width="100" fixed show-overflow-tooltip />
                        <el-table-column v-for="month in last6Months" :key="month" :label="formatMonth(month)" min-width="110" align="right">
                          <template #default="{ row }">
                            <div class="amount-daily">¥ {{ getMonthlyAmount(row, month).toFixed(2) }}</div>
                            <div class="user-count">{{ getMonthlyUserCount(row, month) }} 人</div>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-tab-pane>
                  </el-tabs>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </Transition>
      </div>
    </div>

    <!-- Customer Statistics Module -->
    <div class="dashboard-module customer-module">
      <div class="module-header">
        <el-icon><User /></el-icon>
        <span class="module-title">客户统计</span>
      </div>

      <!-- Customer Summary Cards -->
      <el-row :gutter="20" class="info-row">
        <el-col :xs="24" :sm="12" :md="6" v-for="item in summaryCards" :key="item.title" class="mb-30">
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

      <!-- Customer Charts -->
      <el-row :gutter="20" class="charts-row">
        <el-col :xs="24" :sm="24" :md="16">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>近10日新增客户趋势</span>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import * as echarts from 'echarts';
import { getStats } from '@/api/dashboard';
import { User, ChatDotRound, Collection, Connection, Money, Wallet, Coin } from '@element-plus/icons-vue';

const isSuperAdmin = ref(localStorage.getItem('isSuperAdmin') === 'true');

const stats = ref({
  totalCustomerCount: 0,
  todayNewCustomerCount: 0,
  totalEmployeeCount: 0,
  totalGroupChatCount: 0,
  totalMessageCount: 0,
  customerGrowthTrend: [] as any[],
  addWayDistribution: [] as any[],
  yuewenRecharge: {
    lastMonthAmount: 0,
    todayAmount: 0,
    monthAmount: 0,
    productStats: [] as any[]
  },
  changduRecharge: {
    lastMonthAmount: 0,
    todayAmount: 0,
    monthAmount: 0,
    productStats: [] as any[]
  },
  yuewenHalfYearRecharge: {
    productStats: [] as any[]
  },
  changduHalfYearRecharge: {
    productStats: [] as any[]
  },
  rechargeTrend: [] as any[],
  rechargeHalfYearTrend: [] as any[]
});

const activeProductTab = ref('yuewen');
const rechargePeriodTab = ref('daily');
const activeHalfYearProductTab = ref('yuewen');

const last6Months = computed(() => {
  const yuewenHalfYear = stats.value.yuewenHalfYearRecharge?.productStats || [];
  const changduHalfYear = stats.value.changduHalfYearRecharge?.productStats || [];
  
  let allMonths = [];
  if (yuewenHalfYear.length > 0 && yuewenHalfYear[0].monthlyStats) {
    allMonths = yuewenHalfYear[0].monthlyStats.map((d: any) => d.month);
  } else if (changduHalfYear.length > 0 && changduHalfYear[0].monthlyStats) {
    allMonths = changduHalfYear[0].monthlyStats.map((d: any) => d.month);
  } else {
    for (let i = 5; i >= 0; i--) {
      const d = new Date();
      d.setMonth(d.getMonth() - i);
      allMonths.push(d.toISOString().split('T')[0].substring(0, 7));
    }
  }
  return allMonths;
});

const formatMonth = (monthStr: string) => {
  if (!monthStr) return '';
  const parts = monthStr.split('-');
  return parts.length >= 2 ? `${parts[0]}-${parts[1]}` : monthStr;
};

const getMonthlyAmount = (row: any, month: string) => {
  const m = row.monthlyStats?.find((d: any) => d.month === month);
  return m ? m.amount : 0;
};

const getMonthlyUserCount = (row: any, month: string) => {
  const m = row.monthlyStats?.find((d: any) => d.month === month);
  return m ? m.userCount : 0;
};

const handlePeriodTabChange = () => {
  setTimeout(() => {
    rechargeTrendChart?.resize();
    rechargeHalfYearTrendChart?.resize();
  }, 100);
};

const setRechargePeriodTab = (tabName: string) => {
  rechargePeriodTab.value = tabName;
  handlePeriodTabChange();
};

const last10Days = computed(() => {
  const yuewenProducts = stats.value.yuewenRecharge?.productStats || [];
  const changduProducts = stats.value.changduRecharge?.productStats || [];
  
  let allDates = [];
  if (yuewenProducts.length > 0 && yuewenProducts[0].dailyStats) {
    allDates = yuewenProducts[0].dailyStats.map((d: any) => d.date);
  } else if (changduProducts.length > 0 && changduProducts[0].dailyStats) {
    allDates = changduProducts[0].dailyStats.map((d: any) => d.date);
  } else {
    for (let i = 9; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      allDates.push(d.toISOString().split('T')[0]);
    }
  }
  // Return only the first 9 days (excluding today which is the 10th element)
  return allDates.slice(0, 9);
});

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const parts = dateStr.split('-');
  return parts.length >= 3 ? `${parts[1]}-${parts[2]}` : dateStr;
};

const getDailyAmount = (row: any, date: string) => {
  const day = row.dailyStats?.find((d: any) => d.date === date);
  return day ? day.amount : 0;
};

const getDailyUserCount = (row: any, date: string) => {
  const day = row.dailyStats?.find((d: any) => d.date === date);
  return day ? day.userCount : 0;
};

const rechargeCards = computed(() => {
  const today = (stats.value.yuewenRecharge?.todayAmount || 0) + (stats.value.changduRecharge?.todayAmount || 0);
  const month = (stats.value.yuewenRecharge?.monthAmount || 0) + (stats.value.changduRecharge?.monthAmount || 0);
  const lastMonth = (stats.value.yuewenRecharge?.lastMonthAmount || 0) + (stats.value.changduRecharge?.lastMonthAmount || 0);
  return [
    { title: '今日总充值', value: today, icon: Money, color: '#67C23A' },
    { title: '当月总充值', value: month, icon: Wallet, color: '#409EFF' },
    { title: '上个月总充值', value: lastMonth, icon: Coin, color: '#E6A23C' }
  ];
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
const rechargeTrendChartRef = ref<HTMLElement | null>(null);
const rechargeHalfYearTrendChartRef = ref<HTMLElement | null>(null);

let trendChart: echarts.ECharts | null = null;
let sourceChart: echarts.ECharts | null = null;
let rechargeTrendChart: echarts.ECharts | null = null;
let rechargeHalfYearTrendChart: echarts.ECharts | null = null;

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
  if (rechargeTrendChartRef.value) {
    rechargeTrendChart = echarts.init(rechargeTrendChartRef.value);
    rechargeTrendChart.setOption({
      title: { text: '近10日充值趋势', left: 'center', show: false },
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        boxShadow: '0 0 10px rgba(0,0,0,0.1)'
      },
      legend: { bottom: '0%', left: 'center', icon: 'circle', itemWidth: 8, itemHeight: 8 },
      grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.rechargeTrend?.map(i => i.date) || [],
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed' } },
        axisLabel: { color: '#909399' }
      },
      series: [
        {
          name: '总充值',
          data: stats.value.rechargeTrend?.map(i => i.total) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 3, color: '#F56C6C' },
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '阅文',
          data: stats.value.rechargeTrend?.map(i => i.yuewen) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 2, color: '#409EFF' },
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '常读',
          data: stats.value.rechargeTrend?.map(i => i.changdu) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 2, color: '#67C23A' },
          itemStyle: { color: '#67C23A' }
        }
      ]
    });
  }

  if (rechargeHalfYearTrendChartRef.value) {
    rechargeHalfYearTrendChart = echarts.init(rechargeHalfYearTrendChartRef.value);
    rechargeHalfYearTrendChart.setOption({
      title: { text: '近半年充值趋势', left: 'center', show: false },
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        boxShadow: '0 0 10px rgba(0,0,0,0.1)'
      },
      legend: { bottom: '0%', left: 'center', icon: 'circle', itemWidth: 8, itemHeight: 8 },
      grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.rechargeHalfYearTrend?.map(i => i.month) || [],
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#E4E7ED' } },
        axisLabel: { color: '#909399' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed' } },
        axisLabel: { color: '#909399' }
      },
      series: [
        {
          name: '总充值',
          data: stats.value.rechargeHalfYearTrend?.map(i => i.total) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 3, color: '#F56C6C' },
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '阅文',
          data: stats.value.rechargeHalfYearTrend?.map(i => i.yuewen) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 2, color: '#409EFF' },
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '常读',
          data: stats.value.rechargeHalfYearTrend?.map(i => i.changdu) || [],
          type: 'line',
          smooth: true,
          lineStyle: { width: 2, color: '#67C23A' },
          itemStyle: { color: '#67C23A' }
        }
      ]
    });
  }

  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption({
      title: { text: '近10日新增客户趋势', left: 'center', show: false },
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderWidth: 0,
        boxShadow: '0 0 10px rgba(0,0,0,0.1)'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.customerGrowthTrend?.map(i => i.date) || [],
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
        data: stats.value.customerGrowthTrend?.map(i => i.count) || [],
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
        data: stats.value.addWayDistribution || []
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
  rechargeTrendChart?.resize();
  rechargeHalfYearTrendChart?.resize();
};
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.dashboard-module {
  margin-bottom: 40px;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-left: 4px;
}

.recharge-module .module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Premium Segmented Control (iOS capsule style) */
.segmented-control {
  position: relative;
  display: inline-flex;
  background: #e2e8f0;
  border-radius: 20px;
  padding: 3px;
  border: 1px solid #cbd5e1;
  backdrop-filter: blur(10px);
}

.slider-bg {
  position: absolute;
  top: 3px;
  bottom: 3px;
  left: 3px;
  width: calc(50% - 3px);
  background: #ffffff;
  border-radius: 17px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 1px 3px rgba(0, 0, 0, 0.03);
  transition: transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 0;
}

.slider-bg.daily {
  transform: translateX(0);
}

.slider-bg.monthly {
  transform: translateX(100%);
}

.segment-item {
  position: relative;
  z-index: 1;
  padding: 6px 20px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  border-radius: 17px;
  cursor: pointer;
  transition: color 0.3s ease;
  user-select: none;
}

.segment-item:hover {
  color: #0f172a;
}

.segment-item.active {
  color: #409eff;
  font-weight: 600;
}


/* Tab Content Grid Overlay to prevent jumping during transitions */
.tab-content-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  grid-template-rows: auto;
  width: 100%;
}

.transition-container {
  grid-column: 1 / -1;
  grid-row: 1 / -1;
  width: 100%;
  min-width: 0;
}

/* slide-fade transition styles */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: opacity 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(15px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}



.recharge-module :deep(.el-card) {
  border-radius: 16px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.02) !important;
  border: 1px solid rgba(235, 238, 245, 0.7);
  transition: all 0.3s ease;
}

.recharge-module :deep(.el-card:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05) !important;
}

/* Responsive headers on mobile */
@media (max-width: 600px) {
  .recharge-module .module-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .segmented-control {
    width: 100%;
    display: flex;
  }
  .segment-item {
    flex: 1;
    text-align: center;
  }
}

.module-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.module-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  .mb-20 {
    margin-bottom: 20px;
  }
  .mb-30 {
    margin-bottom: 30px;
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

.mb-30 {
  margin-bottom: 30px;
}

.recharge-card .card-icon {
  background-image: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(0,0,0,0.1) 100%);
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

.product-stats-card {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
.product-stats-card :deep(.el-card__body) {
  padding-top: 0;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #ebeef5;
}

.custom-tabs :deep(.el-tabs__header) {
  margin: 0 0 10px;
  padding: 0 20px;
}

.custom-table {
  --el-table-border-color: #ebeef5;
}

.amount-today {
  color: #f56c6c;
  font-weight: 600;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.amount-daily {
  color: #606266;
  font-weight: 500;
}

.amount-month {
  color: #409EFF;
  font-weight: 600;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.user-count {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
  margin-top: 2px;
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

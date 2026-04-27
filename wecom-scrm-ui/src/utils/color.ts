
export const TAG_COLORS = [
  { color: '#409eff', border: '#d9ecff', bg: '#ecf5ff' }, // blue
  { color: '#67c23a', border: '#e1f3d8', bg: '#f0f9eb' }, // green
  { color: '#e6a23c', border: '#faecd8', bg: '#fdf6ec' }, // orange
  { color: '#f56c6c', border: '#fde2e2', bg: '#fef0f0' }, // red
  { color: '#8e44ad', border: '#ebdcf5', bg: '#f5f0fa' }, // purple
  { color: '#e91e63', border: '#fcd2e1', bg: '#fff0f5' }, // pink
  { color: '#11a1ad', border: '#d2f1f3', bg: '#e6f9fa' }, // cyan
  { color: '#ff9800', border: '#ffe8cc', bg: '#fff8e1' }, // gold
  { color: '#009688', border: '#d2e9e7', bg: '#e0f2f1' }, // teal
  { color: '#909399', border: '#e9e9eb', bg: '#f4f4f5' }, // gray
];

const productColorMap = new Map<string | number, number>();
let nextColorIndex = 0;

/**
 * 获取产品标签样式，采用轮询方式分配颜色，避免重复
 * @param id 产品ID或标识
 * @returns 样式对象
 */
export const getProductTagStyle = (id: any) => {
  if (id === null || id === undefined || id === '') {
    return {
      color: '#909399',
      borderColor: '#e9e9eb',
      backgroundColor: '#f4f4f5'
    };
  }

  if (!productColorMap.has(id)) {
    productColorMap.set(id, nextColorIndex);
    nextColorIndex = (nextColorIndex + 1) % TAG_COLORS.length;
  }

  const index = productColorMap.get(id)!;
  const color = TAG_COLORS[index];
  
  return {
    color: color.color,
    borderColor: color.border,
    backgroundColor: color.bg
  };
};

import { gradientImage, seededRandom } from '../utils/format'

/**
 * 演示数据。
 * 当后端（MySQL / Redis / 高德 Key）没就绪时，前端仍然可以完整走通交互，
 * 所有文案与字段结构都与 PostDTO / ShopDTO 保持一致，方便随时切回真实接口。
 */

const KM_PER_DEG_LAT = 110.574

function haversine(lng1, lat1, lng2, lat2) {
  const R = 6371000
  const toRad = (d) => (d * Math.PI) / 180
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

/** 以中心点为原点，按「方位角 + 距离」偏移出一个坐标 */
function offset(centerLng, centerLat, bearingDeg, distanceM) {
  const rad = (bearingDeg * Math.PI) / 180
  const dLat = (distanceM * Math.cos(rad)) / 111320
  const dLng =
    (distanceM * Math.sin(rad)) / (111320 * Math.cos((centerLat * Math.PI) / 180))
  return { lng: centerLng + dLng, lat: centerLat + dLat }
}

const POST_SEEDS = [
  {
    user: '小安安',
    text: '新开的那家手冲咖啡店终于去打卡了，豆子是日晒耶加，尾韵有点像白桃。下午三点的光刚好落在吧台上。',
    street: '望京 SOHO · T2 一层',
    imgs: 3,
    minsAgo: 26
  },
  {
    user: '阿桃',
    text: '楼下这条樱花道开了！今天风大，花瓣铺了一整条路，走两步就想拍照。',
    street: '朝阳公园西门外 · 樱花小道',
    imgs: 4,
    minsAgo: 74
  },
  {
    user: 'Luca',
    text: '夜跑 5 公里打卡。江边的风比昨天凉，跑完整个人清醒得不行，推荐这条路线。',
    street: '滨江步道 3 号入口',
    imgs: 0,
    minsAgo: 152
  },
  {
    user: '一只猫饼',
    text: '街角新来的那只橘猫今天终于肯靠近我了。带了半根鸡胸肉，被它吃干净了。',
    street: '老槐树便利店门口',
    imgs: 1,
    minsAgo: 305
  },
  {
    user: '周小满',
    text: '周末的旧书市集比想象中大，淘到一本 1998 年版的城市地图册，里面还夹着前任主人写的便签。',
    street: '文创园 B 区中庭',
    imgs: 2,
    minsAgo: 520
  },
  {
    user: 'Nova',
    text: '雨后的天空是那种很干净的蓝，站在天桥上看了一会儿，城市突然安静了几秒。',
    street: '东三环人行天桥',
    imgs: 1,
    minsAgo: 880
  },
  {
    user: '麦麦',
    text: '小区门口这家早餐摊的豆腐脑是咸口的，加辣油和虾皮，五块钱吃到站不起来。',
    street: '阳光苑小区南门',
    imgs: 2,
    minsAgo: 1450
  },
  {
    user: '阿哲',
    text: '把阳台收拾了一遍，绿萝换盆、多肉搬到有光的地方。原来养植物最治愈的是这件事本身很慢。',
    street: '美好家园 3 期 12 号楼',
    imgs: 3,
    minsAgo: 2600
  },
  {
    user: '小鹿',
    text: '公司楼下新摆了一台自助鲜花机，九块九一支洋桔梗，下班顺手带了一支回家。',
    street: '金融街 A 座大堂',
    imgs: 1,
    minsAgo: 4200
  },
  {
    user: '江岸',
    text: '沿着老城区骑了一圈，发现巷子里还藏着一家开了二十年的修表铺。老师傅说现在一个月能修十来块。',
    street: '南锣鼓巷南口',
    imgs: 0,
    minsAgo: 6100
  }
]

const SHOP_SEEDS = [
  { name: '山丘手冲咖啡', type: '餐饮服务;咖啡厅', street: '望京 SOHO T2 一层 105', dist: 180 },
  { name: '麦香现烤面包坊', type: '购物服务;烘焙店', street: '阜通东大街 6 号院底商', dist: 320 },
  { name: '老陈豆腐脑', type: '餐饮服务;中式早餐', street: '阳光苑小区南门东侧', dist: 460 },
  { name: '邻里便利超市', type: '购物服务;便利店;7-ELEVEn', street: '广顺北大街 12 号', dist: 610 },
  { name: '安心大药房', type: '医疗保健服务;药店', street: '望京西路 48 号一层', dist: 840 },
  { name: '一碗兰州拉面', type: '餐饮服务;面馆', street: '花家地北里 3 号楼', dist: 1020 },
  { name: '中国石化加油站', type: '生活服务;加油站', street: '京密路 88 号', dist: 1650 },
  { name: '青檐书店', type: '购物服务;书店', street: '酒仙桥路 4 号 798 艺术区', dist: 2100 },
  { name: '滨江健身工作室', type: '体育休闲服务;健身中心', street: '望京园 5 号楼 B1', dist: 2680 },
  { name: '悦然口腔诊所', type: '医疗保健服务;口腔诊所', street: '阜通西大街 20 号 2 层', dist: 3300 }
]

const BEARINGS = [24, 78, 142, 196, 254, 312, 46, 118, 168, 288]

export function mockPosts(center, radius = 5000) {
  const rand = seededRandom(`posts-${center.lng.toFixed(4)}-${center.lat.toFixed(4)}`)
  const out = []
  POST_SEEDS.forEach((seed, i) => {
    // 按索引展开距离，保证不同半径筛选下都有内容
    const dist = Math.round((180 + i * 430) * (0.72 + rand() * 0.62))
    if (dist > radius) return
    const p = offset(center.lng, center.lat, BEARINGS[i % BEARINGS.length] + rand() * 18, dist)
    const images = []
    for (let k = 0; k < seed.imgs; k++) {
      images.push(gradientImage(`${seed.user}-${i}-${k}`))
    }
    out.push({
      id: `mock-post-${i}`,
      userId: seed.user,
      text: seed.text,
      images,
      lng: Number(p.lng.toFixed(6)),
      lat: Number(p.lat.toFixed(6)),
      address: seed.street,
      createdAt: new Date(Date.now() - seed.minsAgo * 60000).toISOString().slice(0, 19),
      distance: dist
    })
  })
  return out.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
}

export function mockShops(center, radius = 3000, keyword = '') {
  const rand = seededRandom(`shops-${center.lng.toFixed(4)}-${center.lat.toFixed(4)}`)
  const kw = String(keyword || '').trim()
  let list = SHOP_SEEDS.map((s, i) => {
    const dist = Math.round(s.dist * (0.9 + rand() * 0.3))
    const p = offset(center.lng, center.lat, BEARINGS[(i * 3) % BEARINGS.length] + rand() * 22, dist)
    return {
      name: s.name,
      address: s.street,
      location: `${p.lng.toFixed(6)},${p.lat.toFixed(6)}`,
      distance: String(dist),
      type: s.type
    }
  }).filter((s) => Number(s.distance) <= radius)

  if (kw) {
    list = list.filter((s) => (s.name + s.type + s.address).includes(kw))
  }
  return list.sort((a, b) => Number(a.distance) - Number(b.distance))
}

/** 演示模式下模拟一次发帖的返回结构（不会真正落库） */
export function mockCreatePost(payload, center) {
  const p = offset(center.lng, center.lat, 40, 120)
  return {
    id: `mock-local-${Date.now()}`,
    userId: payload.userId,
    text: payload.text,
    images: payload.images && payload.images.length ? payload.images : [],
    lng: Number(p.lng.toFixed(6)),
    lat: Number(p.lat.toFixed(6)),
    address: '当前位置附近（演示数据）',
    createdAt: new Date().toISOString().slice(0, 19),
    distance: 120
  }
}

export { haversine, offset }

/** 把经纬度换算成对中心点的相对米数，供雷达图定位用 */
export function relativeMeters(center, lng, lat) {
  const dLat = (lat - center.lat) * KM_PER_DEG_LAT * 1000
  const dLng =
    (lng - center.lng) * KM_PER_DEG_LAT * 1000 * Math.cos((center.lat * Math.PI) / 180)
  return { dx: dLng, dy: dLat }
}

export function arrayTree(arr: any[]) {
  const res: any = []
  const map = new Map()
  arr.forEach((item) => {
    map.set(Number(item.id), item)
  })

  arr.forEach((item) => {
    const parent = Number(item.pid) && map.get(Number(item.pid))
    if (parent) {
      if (parent?.children) {
        parent.children.push(item)
      }
      else {
        parent.children = [item]
      }
    }
    else {
      res.push(item)
    }
  })

  return res
}

import { Point } from "@/domain/game"

import styles from "./cell.module.css";

type CellProps = {
  point: Point,
  canMoveTo?: boolean, 
  onClick?: (x: Point) => void
}

export const CellView = (props: CellProps) => {
  const onCellClick = () => props.onClick && props.onClick(props.point)

  return (
    <div
      className={`${styles.fieldCell} ${props.canMoveTo ? styles.fieldCell_canMove : ''}`}
      onClick={onCellClick}
    />
  )
}
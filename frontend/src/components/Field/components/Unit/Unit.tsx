import Image from "next/image";
import { EntityAndPosition } from "@/domain/game"

import styles from "./unit.module.css";

type UnitViewProps = {
  unit: EntityAndPosition
  selected?: boolean
  onClick?: (unit: EntityAndPosition) => void 
}

export const UnitView = (props: UnitViewProps) => {

  const { unit, selected } = props;

  const onClick = () => props.onClick && props.onClick(unit)

  return (
    <div className={styles.unit}
      style={{top: `${unit.position.y * 10}%`, left: `${unit.position.x * 10}%` }}
      onClick={onClick}>
      <Image
        className={selected ? styles.unit_selected : ''}
        src={`/${unit.entity.player}/${unit.entity.type}.png`}
        alt="unit"
        fill
      />
    </div>
  )
}

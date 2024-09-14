package stratego.domain

import stratego.domain.Entity._
import stratego.domain.Field._

object Field {
  type Entites = Map[Point, Entity]

  case class Point(x: Int, y: Int)

  case class Lake(points: List[Point]) {
    def contains(point: Point): Boolean = this.points.contains(point)
  }
}

case class Field(width: Int,
                 height: Int,
                 entities: Entites,
                 leftLake: Lake,
                 rightLake: Lake) {

  def getEntity(point: Point): Option[Entity] = entities.get(point)

  def isPointInLake(point: Point): Boolean = leftLake.contains(point) || rightLake.contains(point)

  def removeEntity(toRemove: Entity): Field =
    this.copy(entities = entities.filter { case (_, entity) =>  entity.id != toRemove.id })

  def moveEntity(toMove: Entity, nextPoint: Point): Field = {
    val nextEntities = entities.filter { case (_, entity) =>  entity.id != toMove.id }
    this.copy(entities = nextEntities + ((nextPoint, toMove)))
  }

  def getPossibleMovements(entity: Entity): List[Point] =
    entities.find { case (_, e) => e.id == entity.id } match {
      case None => List.empty
      case Some((p, e)) =>
        e match {
          case flag: Flag => List.empty
          case mine: Mine => List.empty
          case unit: Entity.Unit =>
            List(Point(p.x - 1, p.y), Point(p.x, p.y + 1), Point(p.x + 1, p.y), Point(p.x, p.y - 1))
              .filterNot(p => p.x < 0 || p.x > width - 1 || p.y < 0 || p.y > height - 1)
        }
    }

}

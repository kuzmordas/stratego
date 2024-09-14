package stratego.domain

import stratego.domain.GameState._
import stratego.domain.Entity._
import scala.collection.immutable
import java.time.Instant
import java.util.UUID
import stratego.domain.Player.Green
import stratego.domain.Player.Red
import scala.collection.mutable
import scala.util.Random
import scala.annotation.tailrec

object GameState {

  sealed trait Event
  case class Drowning(id: UUID, unit: Entity.Unit) extends Event
  case class Moved(id: UUID, unit: Entity.Unit) extends Event
  case class Kill(id: UUID, attacking: Entity, killed: Entity) extends Event
  case class Die(id: UUID, defending: Entity, killed: Entity) extends Event
  case class BothDie(id: UUID, attacking: Entity, defending: Entity) extends Event
  case class DisactivateMine(id: UUID, sapper: Sapper, mine: Mine) extends Event
  case class KillMarshal(id: UUID, spy: Spy, marshal: Marhsal) extends Event
  case class CaptureFlag(id: UUID, unit: Entity.Unit, flag: Entity.Flag) extends Event
  case object Nothing extends Event

  case class Move(player: Player, entity: Entity, to: Field.Point)

  case class Result(state: GameState, event: Event)
  object Result {
    def nothing(state: GameState) = Result(state, Nothing)
  }

  def switchPlayer(player: Player): Player = player match {
    case Player.Green => Player.Red
    case Player.Red => Player.Green
  }

  def make(width: Int, height: Int, entities: Option[Field.Entites]): GameState = {
    val armies = entities.getOrElse(generateEntities(width, height))
    val leftLake = Field.Lake(
      List(
        Field.Point(2, 4),
        Field.Point(3, 4),
        Field.Point(2, 5),
        Field.Point(3, 5),
      )
    )
    val rightLake = Field.Lake(
      List(
        Field.Point(6, 4),
        Field.Point(7, 4),
        Field.Point(6, 5),
        Field.Point(7, 5),
      )
    )
    val field = Field(width, height, armies, leftLake, rightLake)
    GameState(Player.Green, field)
  }

  def generateEntities(width: Int, height: Int) = {
    @tailrec
    def randomise(units: List[Entity], points: List[Field.Point], acc: Map[Field.Point, Entity] = Map.empty): Map[Field.Point, Entity] = 
      units match {
        case unit :: next =>
          val point = unit match {
            case flag: Flag =>
              flag.player match {
                case Green => Field.Point(Random.between(0, 9), height - 1)
                case Red => Field.Point(Random.between(0, 9), 0)
              }
            case u =>
              points(Random.between(0, points.size))
          }
          
          randomise(next, points.filter(p => p != point),  acc + (point -> unit))
        case immutable.Nil => acc
      }


    val greenPoints = List(
      Field.Point(0, height - 1),
      Field.Point(1, height - 1),
      Field.Point(2, height - 1),
      Field.Point(3, height - 1),
      Field.Point(4, height - 1),
      Field.Point(5, height - 1),
      Field.Point(6, height - 1),
      Field.Point(7, height - 1),
      Field.Point(8, height - 1),
      Field.Point(9, height - 1),
      Field.Point(0, height - 2),
      Field.Point(1, height - 2),
      Field.Point(2, height - 2),
      Field.Point(3, height - 2),
      Field.Point(4, height - 2),
      Field.Point(5, height - 2),
      Field.Point(6, height - 2),
      Field.Point(7, height - 2),
      Field.Point(8, height - 2),
      Field.Point(9, height - 2),
    )

    val redPoints = List(
      Field.Point(0, 0),
      Field.Point(1, 0),
      Field.Point(2, 0),
      Field.Point(3, 0),
      Field.Point(4, 0),
      Field.Point(5, 0),
      Field.Point(6, 0),
      Field.Point(7, 0),
      Field.Point(8, 0),
      Field.Point(9, 0),
      Field.Point(0, 1),
      Field.Point(1, 1),
      Field.Point(2, 1),
      Field.Point(3, 1),
      Field.Point(4, 1),
      Field.Point(5, 1),
      Field.Point(6, 1),
      Field.Point(7, 1),
      Field.Point(8, 1),
      Field.Point(9, 1),
    )

    val greenUnits: List[Entity] = List(
      Flag(201, Player.Green), // should be always first
      Marhsal(202, Player.Green),
      Marhsal(203, Player.Green),
      General(204, Player.Green),
      General(205, Player.Green),
      Colonel(206, Player.Green),
      Colonel(207, Player.Green),
      Major(208, Player.Green),
      Major(209, Player.Green),
      Captain(210, Player.Green),
      Captain(211, Player.Green),
      Lieutenant(212, Player.Green),
      Lieutenant(213, Player.Green),
      Sergeant(214, Player.Green),
      Sergeant(215, Player.Green),
      Sapper(216, Player.Green),
      Sapper(217, Player.Green),
      Soldier(218, Player.Green),
      Soldier(219, Player.Green),
      Mine(220, Player.Green)
    )

    val redUnits: List[Entity] = List(
      Flag(101, Player.Red), // should be always first
      Marhsal(102, Player.Red),
      Marhsal(103, Player.Red),
      General(104, Player.Red),
      General(105, Player.Red),
      Colonel(106, Player.Red),
      Colonel(107, Player.Red),
      Major(108, Player.Red),
      Major(109, Player.Red),
      Captain(110, Player.Red),
      Captain(111, Player.Red),
      Lieutenant(112, Player.Red),
      Lieutenant(113, Player.Red),
      Sergeant(114, Player.Red),
      Sergeant(115, Player.Red),
      Sapper(116, Player.Red),
      Sapper(117, Player.Red),
      Soldier(118, Player.Red),
      Soldier(119, Player.Red),
      Mine(120, Player.Red)
    )

    val red = randomise(redUnits, redPoints)
    val green = randomise(greenUnits, greenPoints)
    
    red ++ green
  }

  case class HiddenEntity(id: Int, player: Player)
  type HiddenEntites = Map[Field.Point, Either[HiddenEntity, Entity]]

  case class HiddenField(width: Int,
                         height: Int,
                         entities: HiddenEntites,
                         leftLake: Field.Lake,
                         rightLake: Field.Lake)

  final case class Hidden(currentPlayer: Player, field: HiddenField, winner: Option[Player] = None)

  object Hidden {

    def fromState(state: GameState, hiddenPlayer: Player): Hidden =
      Hidden(
        state.currentPlayer,
        HiddenField(
          state.field.width,
          state.field.height,
          state.field.entities.foldLeft[HiddenEntites](Map.empty)((acc, curr) => {
            if (curr._2.player == hiddenPlayer) acc + ((curr._1, Left(HiddenEntity(curr._2.id, curr._2.player))))
            else acc + ((curr._1, Right(curr._2)))
          }),
          state.field.leftLake,
          state.field.rightLake
        ),
        state.winner
      )
  }
}

case class GameState(currentPlayer: Player, field: Field, winner: Option[Player] = None) {

  def getMovements(entity: Entity): List[Field.Point] =
    field
      .getPossibleMovements(entity)
      .filter(p => field.getEntity(p) match {
        case None => true
        case Some(e) => e.player != currentPlayer
      })
  
  def execute(move: Move): Result =
    if (move.player != currentPlayer) Result.nothing(nothingChanged)
    else if (!getMovements(move.entity).contains(move.to)) Result.nothing(nothingChanged)
    else move.entity match {
      case flag: Flag => Result.nothing(nothingChanged)
      case mine: Mine => Result.nothing(nothingChanged)
      case attacking: Entity.Unit => 
        field.getEntity(move.to) match {
          case None => 
            if (field.isPointInLake(move.to))
              Result(nextTurn(field.removeEntity(move.entity)), Drowning(UUID.randomUUID(), attacking))
            else
              Result(nextTurn(field = field.moveEntity(move.entity, move.to)), Moved(UUID.randomUUID(), attacking))
          case Some(entity) =>
            if (entity.player == currentPlayer) Result.nothing(nothingChanged)
            else entity match {
              case flag: Flag => 
                Result(
                  win(field.removeEntity(flag).moveEntity(attacking, move.to), currentPlayer),
                  CaptureFlag(UUID.randomUUID(), attacking, flag)
                )
              case mine: Mine =>
                attacking match {
                  case attacking: Sapper =>
                    Result(
                      nextTurn(field.removeEntity(mine).moveEntity(attacking, move.to)),
                      DisactivateMine(UUID.randomUUID(), attacking, mine)  
                    )
                  case unit =>
                    Result(
                      nextTurn(field.removeEntity(unit).removeEntity(mine)),
                      BothDie(UUID.randomUUID(), attacking, mine)  
                    )
                }
              case defending: Entity.Unit =>
                defending match {
                  case marshal: Marhsal =>
                    attacking match {
                      case spy: Spy =>
                        Result(
                          nextTurn(field.removeEntity(marshal).moveEntity(spy, move.to)),
                          KillMarshal(UUID.randomUUID(), spy, marshal)
                        )
                      case unit: Marhsal =>
                        Result(
                          nextTurn(field.removeEntity(marshal).removeEntity(unit)),
                          BothDie(UUID.randomUUID(), unit, marshal)
                        )
                      case unit =>
                        Result(
                          nextTurn(field.removeEntity(attacking)),
                          Die(UUID.randomUUID(), marshal, unit)
                        )
                    }
                  case defending =>
                    if (attacking.force < defending.force)
                      Result(
                        nextTurn(field.removeEntity(defending).moveEntity(attacking, move.to)),
                        Kill(UUID.randomUUID(), attacking, defending)
                      )
                    else if (attacking.force > defending.force)
                      Result(
                        nextTurn(field.removeEntity(attacking)),
                        Die(UUID.randomUUID(), defending, attacking)
                      )
                    else
                      Result(
                        nextTurn(field.removeEntity(attacking).removeEntity(defending)),
                        BothDie(UUID.randomUUID(), attacking, defending)
                      )
                }
              }
            }
        }

  private def nothingChanged: GameState = this

  private def nextTurn(field: Field): GameState = {
    val winner = if (checkIfPlayerWin(field, Player.Green)) Some(Player.Green)
    else if (checkIfPlayerWin(field, Player.Red)) Some(Player.Green)
    else None
    
    this.copy(currentPlayer = switchPlayer(currentPlayer), field, winner)
  }

  private def checkIfPlayerWin(field: Field, player: Player): Boolean = {
    val opositePlayer = player match {
      case Green => Red
      case Red => Green
    }

    field.entities
      .filter { case (_, e) => e.player == opositePlayer }
      .forall { case (_, e) => e match {
        case e @ (Flag(_, _) | Mine(_, _)) => true
        case _ => false
      }}
  }

  private def win(finalField: Field, player: Player): GameState =
    this.copy(field = finalField, winner = Some(player))
}

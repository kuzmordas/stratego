package stratego.http.application.data

import stratego.domain._
import stratego.domain.Player.{Green, Red}
import stratego.domain.GameState._
import stratego.domain.Field.Lake
import stratego.domain.Entity._
import stratego.http.domain._

import zio.json._
import scala.collection.immutable
import scala.annotation.tailrec
import java.util.UUID
import zio.json.ast.Json
import zio.json.ast.JsonCursor


object domain {

  def playerFromString(value: String): Either[String, Player] = value match {
    case "red" => Right(Player.Red)
    case "green" => Right(Player.Green)
    case s => Left(s"wrong player type: $s")
  }

  def playerToString(player: Player) = player match {
    case Green => "green"
    case Red => "red"
  }

  implicit val pointDecoder: JsonDecoder[Field.Point] = DeriveJsonDecoder.gen[Field.Point]
  implicit val pointEncoder: JsonEncoder[Field.Point] = DeriveJsonEncoder.gen[Field.Point]

  implicit val lakeDecoder: JsonDecoder[Field.Lake] = DeriveJsonDecoder.gen[Field.Lake]
  implicit val lakeEncoder: JsonEncoder[Field.Lake] = DeriveJsonEncoder.gen[Field.Lake]

  implicit val playerDecoder: JsonDecoder[Player] = JsonDecoder[String].mapOrFail(playerFromString)
  implicit val playerEncoder: JsonEncoder[Player] = JsonEncoder[String].contramap[Player](playerToString)

  implicit val userWithoutPlayerDecoder: JsonDecoder[User.WithoutPlayer] = DeriveJsonDecoder.gen[User.WithoutPlayer]
  implicit val userWithoutPlayerEncoder: JsonEncoder[User.WithoutPlayer] = DeriveJsonEncoder.gen[User.WithoutPlayer]

  implicit val userDecoder: JsonDecoder[User.WithPlayer] = DeriveJsonDecoder.gen[User.WithPlayer]
  implicit val userEncoder: JsonEncoder[User.WithPlayer] = DeriveJsonEncoder.gen[User.WithPlayer]

  case class EntityDto(`type`: String, id: Int, player: Player, force: Option[Int])
  object EntityDto {
    implicit val decoder: JsonDecoder[EntityDto] = DeriveJsonDecoder.gen[EntityDto]
    implicit val encoder: JsonEncoder[EntityDto] = DeriveJsonEncoder.gen[EntityDto]

    def fromDomain(e: Entity) = e match {
      case e: Flag => EntityDto("flag", e.id, e.player, None)
      case e: Mine => EntityDto("mine", e.id, e.player, None)
      case e: Marhsal => EntityDto("marshal", e.id, e.player, Some(e.force))
      case e: Major => EntityDto("major", e.id, e.player, Some(e.force))
      case e: Lieutenant => EntityDto("lieutenant", e.id, e.player, Some(e.force))
      case e: Captain => EntityDto("captain", e.id, e.player, Some(e.force))
      case e: Sapper => EntityDto("sapper", e.id, e.player, Some(e.force))
      case e: Spy => EntityDto("spy", e.id, e.player, Some(e.force))
      case e: Soldier => EntityDto("soldier", e.id, e.player, Some(e.force))
      case e: Colonel => EntityDto("colonel", e.id, e.player, Some(e.force))
      case e: Sergeant => EntityDto("sergeant", e.id, e.player, Some(e.force))
      case e: General => EntityDto("general", e.id, e.player, Some(e.force))
    }

    def toDomain(dto: EntityDto): Either[String, Entity] = dto.`type` match {
      case "flag" => Right(Entity.Flag(dto.id, dto.player))
      case "mine" => Right(Entity.Mine(dto.id, dto.player))
      case "marshal" => Right(Entity.Marhsal(dto.id, dto.player))
      case "major" => Right(Entity.Major(dto.id, dto.player))
      case "lieutenant" => Right(Entity.Lieutenant(dto.id, dto.player))
      case "captain" => Right(Entity.Captain(dto.id, dto.player))
      case "sapper" => Right(Entity.Sapper(dto.id, dto.player))
      case "spy" => Right(Entity.Spy(dto.id, dto.player))
      case "soldier" => Right(Entity.Soldier(dto.id, dto.player))
      case "colonel" => Right(Entity.Colonel(dto.id, dto.player))
      case "sergeant" => Right(Entity.Sergeant(dto.id, dto.player))
      case "general" => Right(Entity.General(dto.id, dto.player))
      case e => Left(s"wrong entity type: $e")  
    }
  }

  implicit val entityDecoder: JsonDecoder[Entity] = DeriveJsonDecoder.gen[EntityDto].mapOrFail(EntityDto.toDomain)
  implicit val entityEncoder: JsonEncoder[Entity] = DeriveJsonEncoder.gen[EntityDto].contramap[Entity](EntityDto.fromDomain)
  
  implicit val moveDecoder: JsonDecoder[Move] = DeriveJsonDecoder.gen[Move]
  implicit val moveEncoder: JsonEncoder[Move] = DeriveJsonEncoder.gen[Move]

  case class EntityAndPositionDto(entity: EntityDto, position: Field.Point)
  implicit val entityAndPositionDecoder: JsonDecoder[EntityAndPositionDto] = DeriveJsonDecoder.gen[EntityAndPositionDto]
  implicit val entityAndPositionEncoder: JsonEncoder[EntityAndPositionDto] = DeriveJsonEncoder.gen[EntityAndPositionDto]

  case class HiddenEntityDto()
  implicit val hiddenEntityDtoDecoder: JsonDecoder[HiddenEntityDto] = DeriveJsonDecoder.gen[HiddenEntityDto]
  implicit val hiddenEntityDtoEncoder: JsonEncoder[HiddenEntityDto] = DeriveJsonEncoder.gen[HiddenEntityDto]

  case class EntityOrHiddenAndPositionDto(entity: Either[HiddenEntityDto, EntityDto], position: Field.Point)
  implicit val entityOrHiddenAndPositionDecoder: JsonDecoder[EntityOrHiddenAndPositionDto] = DeriveJsonDecoder.gen[EntityOrHiddenAndPositionDto]
  implicit val entityOrHiddenAndPositionEncoder: JsonEncoder[EntityOrHiddenAndPositionDto] = DeriveJsonEncoder.gen[EntityOrHiddenAndPositionDto]

  case class FieldDto(width: Int,
                      height: Int,
                      entities: List[EntityAndPositionDto],
                      leftLake: Lake, 
                      rightLake: Lake)

  object FieldDto {

    @tailrec
    private def rec(l: List[EntityAndPositionDto], acc: Map[Field.Point, Entity] = Map.empty): Either[String, Map[Field.Point, Entity]] = 
      l match {
        case head :: rest => 
          EntityDto.toDomain(head.entity) match {
            case Left(err) => Left(err)
            case Right(e) => 
              val next = acc + (head.position -> e)
              rec(rest, next)
          }

        case Nil => Right(acc)
      }

    def toDomain(dto: FieldDto): Either[String, Field] =
      rec(dto.entities)
        .map(entities =>
            Field(
              dto.width,
              dto.height,
              entities,
              dto.leftLake,
              dto.rightLake
           )
        )

    def fromDomain(domain: Field): FieldDto = 
      FieldDto(
        domain.width,
        domain.height,
        domain.entities.map { case (p, e) => EntityAndPositionDto(EntityDto.fromDomain(e), p) }.toList,
        domain.leftLake,
        domain.rightLake,
      )
  }                  
  
  implicit val fieldDecoder: JsonDecoder[Field] = DeriveJsonDecoder.gen[FieldDto].mapOrFail(FieldDto.toDomain)
  implicit val fieldEncoder: JsonEncoder[Field] = DeriveJsonEncoder.gen[FieldDto].contramap(FieldDto.fromDomain)

  implicit val gameStateDecoder: JsonDecoder[GameState] = DeriveJsonDecoder.gen[GameState]
  implicit val gameStateEncoder: JsonEncoder[GameState] = DeriveJsonEncoder.gen[GameState]


  case class HiddenFieldDto(width: Int,
                            height: Int,
                            entities: List[EntityAndPositionDto],
                            leftLake: Lake, 
                            rightLake: Lake)

  object HiddenFieldDto {

    @tailrec
    private def rec(l: List[EntityAndPositionDto], acc: Map[Field.Point, Either[HiddenEntity, Entity]] = Map.empty): Either[String, Map[Field.Point, Either[HiddenEntity, Entity]]] = 
      l match {
        case head :: rest =>
          if (head.entity.`type` == "hidden") {
            val next = acc + (head.position -> Left(HiddenEntity(head.entity.id, head.entity.player)))
            rec(rest, next)
          } else {
            EntityDto.toDomain(head.entity) match {
                case Left(err) => Left(err)
                case Right(e) => 
                  val next = acc + (head.position -> Right(e))
                  rec(rest, next)
              }
          }
        case Nil => Right(acc)
      }

    def toDomain(dto: HiddenFieldDto): Either[String, GameState.HiddenField] =
      rec(dto.entities)
        .map(entities =>
            GameState.HiddenField(
              dto.width,
              dto.height,
              entities,
              dto.leftLake,
              dto.rightLake
           )
        )

    def fromDomain(domain: GameState.HiddenField): HiddenFieldDto = 
      HiddenFieldDto(
        domain.width,
        domain.height,
        domain.entities.map { case (p, e) => 
          e match {
            case Left(value) => EntityAndPositionDto(EntityDto("hidden", value.id, value.player, None), p)
            case Right(value) => EntityAndPositionDto(EntityDto.fromDomain(value), p)
          }}.toList,
        domain.leftLake,
        domain.rightLake,
      )
  }

  implicit val hiddenFieldDecoder: JsonDecoder[GameState.HiddenField] = DeriveJsonDecoder.gen[HiddenFieldDto].mapOrFail(HiddenFieldDto.toDomain)
  implicit val hiddenFieldEncoder: JsonEncoder[GameState.HiddenField] = DeriveJsonEncoder.gen[HiddenFieldDto].contramap(HiddenFieldDto.fromDomain)

  implicit val gameStateHiddenStateDecoder: JsonDecoder[GameState.Hidden] = DeriveJsonDecoder.gen[GameState.Hidden]
  implicit val gameStateHiddenStateEncoder: JsonEncoder[GameState.Hidden] = DeriveJsonEncoder.gen[GameState.Hidden]

  @jsonDiscriminator("type")
  sealed trait EventDto

  @jsonHint("drowning")
  case class DrowningDto(id: UUID, unit: EntityDto) extends EventDto

  @jsonHint("moved")
  case class MovedDto(id: UUID, unit: EntityDto) extends EventDto

  @jsonHint("kill")
  case class KillDto(id: UUID, attacking: EntityDto, killed: EntityDto) extends EventDto

  @jsonHint("die")
  case class DieDto(id: UUID, defending: EntityDto, killed: EntityDto) extends EventDto

  @jsonHint("bothDie")
  case class BothDieDto(id: UUID, attacking: EntityDto, defending: EntityDto) extends EventDto

  @jsonHint("disactiveMine")
  case class DisactivateMineDto(id: UUID, sapper: EntityDto, mine: EntityDto) extends EventDto

  @jsonHint("killMarshal")
  case class KillMarshalDto(id: UUID, spy: EntityDto, marshal: EntityDto) extends EventDto

  @jsonHint("captureFlag")
  case class CaptureFlagDto(id: UUID, unit: EntityDto, flag: EntityDto) extends EventDto

  @jsonHint("nothing")
  case class NothingDto() extends EventDto

  object EventDto {

    def fromDomain(domain: Event): EventDto = domain match {
      case e: BothDie => 
        BothDieDto(e.id, EntityDto.fromDomain(e.attacking), EntityDto.fromDomain(e.defending))
      case e: DisactivateMine =>
        DisactivateMineDto(e.id, EntityDto.fromDomain(e.sapper), EntityDto.fromDomain(e.mine))
      case e: CaptureFlag =>
        CaptureFlagDto(e.id, EntityDto.fromDomain(e.unit), EntityDto.fromDomain(e.flag))
      case e: Kill =>
        KillDto(e.id, EntityDto.fromDomain(e.attacking), EntityDto.fromDomain(e.killed))
      case e: KillMarshal =>
        KillMarshalDto(e.id, EntityDto.fromDomain(e.spy), EntityDto.fromDomain(e.marshal))
      case e: Drowning =>
        DrowningDto(e.id, EntityDto.fromDomain(e.unit))
      case e: Moved =>
        MovedDto(e.id, EntityDto.fromDomain(e.unit))
      case e: Die =>
        DieDto(e.id, EntityDto.fromDomain(e.defending), EntityDto.fromDomain(e.killed))
      case e: GameState.Nothing.type =>
        NothingDto()
    }

    def toDomain(dto: EventDto): Either[String, Event] = dto match {
      case dto: DrowningDto => 
        EntityDto.toDomain(dto.unit).flatMap {
          case e: Entity.Unit => Right(GameState.Drowning(dto.id, e))
          case _ => Left("wrong unit type for drowning event")
        }

      case dto: MovedDto =>
        EntityDto.toDomain(dto.unit).flatMap {
          case e: Entity.Unit => Right(GameState.Moved(dto.id, e))
          case _ => Left("wrong unit type for moved event")
        }

      case dto: KillDto =>
        for {
          attacking <- EntityDto.toDomain(dto.attacking)
          killed <- EntityDto.toDomain(dto.killed)
        } yield GameState.Kill(dto.id, attacking, killed)

      case dto: DieDto =>
        for {
          defending <- EntityDto.toDomain(dto.defending)
          killed <- EntityDto.toDomain(dto.killed)
        } yield GameState.Die(dto.id, defending, killed)

      case dto: BothDieDto =>
        for {
          attacking <- EntityDto.toDomain(dto.attacking)
          defending <- EntityDto.toDomain(dto.defending)
        } yield GameState.BothDie(dto.id, attacking, defending)

      case dto: DisactivateMineDto => 
        for {
          mine <- EntityDto.toDomain(dto.mine).flatMap {
            case e: Entity.Mine => Right(e)
            case _ => Left("wrong unit type for mine")
          }
          sapper <- EntityDto.toDomain(dto.sapper).flatMap {
            case e: Entity.Sapper => Right(e)
            case _ => Left("wrong unit type for sapper")
          }
        } yield GameState.DisactivateMine(dto.id, sapper, mine)
        

      case dto: KillMarshalDto =>
        for {
          marshal <- EntityDto.toDomain(dto.marshal).flatMap {
            case e: Entity.Marhsal => Right(e)
            case _ => Left("wrong unit type for marshal")
          }
          spy <- EntityDto.toDomain(dto.spy).flatMap {
            case e: Entity.Spy => Right(e)
            case _ => Left("wrong unit type for spy")
          }
        } yield GameState.KillMarshal(dto.id, spy, marshal)

      case dto: CaptureFlagDto =>
        for {
          unit <- EntityDto.toDomain(dto.unit).flatMap {
            case e: Entity.Unit => Right(e)
            case _ => Left("wrong unit type for unit")
          }
          flag <- EntityDto.toDomain(dto.flag).flatMap {
            case e: Entity.Flag => Right(e)
            case _ => Left("wrong unit type for flag")
          }
        } yield GameState.CaptureFlag(dto.id, unit, flag)

      case dto: NothingDto =>
        Right(GameState.Nothing)
    }

  }


  implicit val eventDecoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[EventDto].mapOrFail(EventDto.toDomain)
  implicit val eventEncoder: JsonEncoder[Event] = DeriveJsonEncoder.gen[EventDto].contramap(EventDto.fromDomain)

  implicit val inactiveSessionDecoder: JsonDecoder[Session.Inactive] = DeriveJsonDecoder.gen[Session.Inactive]
  implicit val inactiveSessionEncoder: JsonEncoder[Session.Inactive] = DeriveJsonEncoder.gen[Session.Inactive]
  
  implicit val activeSessionDecoder: JsonDecoder[Session.Active] = DeriveJsonDecoder.gen[Session.Active]
  implicit val activeSessionEncoder: JsonEncoder[Session.Active] = DeriveJsonEncoder.gen[Session.Active]

  implicit val sessionWithHiddenStateDecoder: JsonDecoder[Session.WithHiddenState] = DeriveJsonDecoder.gen[Session.WithHiddenState]
  implicit val sessionWithHiddenStateEncoder: JsonEncoder[Session.WithHiddenState] = DeriveJsonEncoder.gen[Session.WithHiddenState]

}

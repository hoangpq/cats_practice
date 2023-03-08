import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.collection.Contains
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object Types {
  type UserNameR = NonEmptyString
  object UserNameR extends RefinedTypeOps[UserNameR, String]
  type NameR = NonEmptyString

  object NameR extends RefinedTypeOps[NameR, String]
  type EmailR = String Refined Contains['@']
  object EmailR extends RefinedTypeOps[EmailR, String]
  @newtype case class UserName(value: UserNameR)
  @newtype case class Name(value: NameR)
  @newtype case class Email(value: EmailR)
}
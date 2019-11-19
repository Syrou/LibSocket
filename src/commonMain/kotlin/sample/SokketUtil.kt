package sample

class SokketUtil {
    companion object{
        fun isValidIP4Adress(input:String):Boolean{
            val pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\$"
            return pattern.toRegex().find(input)?.groups?.isNotEmpty() ?: false
        }
    }
}
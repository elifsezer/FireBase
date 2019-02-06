package com.example.firebasekotlin

class SohbetOdasi {

    var sohbetodasi_adi: String? = null
    var olusturan_id: String? = null
    var seviye: String? = null
    var sohbetodasi_id: String? = null


    constructor() {}

    constructor(sohbetodasi_adi: String, olusturan_id: String, seviye: String, sohbetodasi_id: String) {
        this.sohbetodasi_adi = sohbetodasi_adi
        this.olusturan_id = olusturan_id
        this.seviye = seviye
        this.sohbetodasi_id = sohbetodasi_id
    }
}

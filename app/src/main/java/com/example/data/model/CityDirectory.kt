package com.example.data.model

object CityDirectory {

    val CITIES: List<CityInfo> = listOf(
        // Pakistan
        CityInfo("pk_khi", "Karachi", "کراچی", "Pakistan", "پاکستان", 24.8607, 67.0011, "Asia/Karachi"),
        CityInfo("pk_lhe", "Lahore", "لاہور", "Pakistan", "پاکستان", 31.5497, 74.3436, "Asia/Karachi"),
        CityInfo("pk_isb", "Islamabad", "اسلام آباد", "Pakistan", "پاکستان", 33.6844, 73.0479, "Asia/Karachi"),
        CityInfo("pk_rwp", "Rawalpindi", "راولپنڈی", "Pakistan", "پاکستان", 33.5651, 73.0169, "Asia/Karachi"),
        CityInfo("pk_fsd", "Faisalabad", "فیصل آباد", "Pakistan", "پاکستان", 31.4504, 73.1350, "Asia/Karachi"),
        CityInfo("pk_mux", "Multan", "ملتان", "Pakistan", "پاکستان", 30.1575, 71.5249, "Asia/Karachi"),
        CityInfo("pk_pew", "Peshawar", "پشاور", "Pakistan", "پاکستان", 34.0151, 71.5249, "Asia/Karachi"),
        CityInfo("pk_uet", "Quetta", "کوئٹہ", "Pakistan", "پاکستان", 30.1798, 66.9750, "Asia/Karachi"),
        CityInfo("pk_skt", "Sialkot", "سیالکوٹ", "Pakistan", "پاکستان", 32.4945, 74.5229, "Asia/Karachi"),
        CityInfo("pk_hyd", "Hyderabad", "حیدرآباد", "Pakistan", "پاکستان", 25.3960, 68.3578, "Asia/Karachi"),
        CityInfo("pk_gwn", "Gujranwala", "گوجرانوالہ", "Pakistan", "پاکستان", 32.1877, 74.1945, "Asia/Karachi"),

        // Saudi Arabia & Gulf
        CityInfo("sa_mak", "Makkah", "مکہ مکرمہ", "Saudi Arabia", "سعودی عرب", 21.4225, 39.8262, "Asia/Riyadh"),
        CityInfo("sa_med", "Madinah", "مدینہ منورہ", "Saudi Arabia", "سعودی عرب", 24.5247, 39.5692, "Asia/Riyadh"),
        CityInfo("sa_ruh", "Riyadh", "ریاض", "Saudi Arabia", "سعودی عرب", 24.7136, 46.6753, "Asia/Riyadh"),
        CityInfo("sa_jed", "Jeddah", "جدہ", "Saudi Arabia", "سعودی عرب", 21.5433, 39.1728, "Asia/Riyadh"),
        CityInfo("ae_dxb", "Dubai", "دبئی", "United Arab Emirates", "متحدہ عرب امارات", 25.2048, 55.2708, "Asia/Dubai"),
        CityInfo("ae_auh", "Abu Dhabi", "ابوظہبی", "United Arab Emirates", "متحدہ عرب امارات", 24.4539, 54.3773, "Asia/Dubai"),
        CityInfo("qa_doh", "Doha", "دوحہ", "Qatar", "قطر", 25.2854, 51.5310, "Asia/Qatar"),
        CityInfo("kw_kwi", "Kuwait City", "کویت سٹی", "Kuwait", "کویت", 29.3759, 47.9774, "Asia/Kuwait"),
        CityInfo("bh_bah", "Manama", "منامہ", "Bahrain", "بحرین", 26.2285, 50.5860, "Asia/Bahrain"),
        CityInfo("om_mct", "Muscat", "مسقط", "Oman", "عمان", 23.5880, 58.3829, "Asia/Muscat"),

        // Middle East & North Africa
        CityInfo("tr_ist", "Istanbul", "استنبول", "Turkey", "ترکیہ", 41.0082, 28.9784, "Europe/Istanbul"),
        CityInfo("eg_cai", "Cairo", "قاہرہ", "Egypt", "مصر", 30.0444, 31.2357, "Africa/Cairo"),
        CityInfo("jo_amm", "Amman", "عمان", "Jordan", "اردن", 31.9454, 35.9284, "Asia/Amman"),
        CityInfo("ps_jer", "Jerusalem (Al-Quds)", "القدس الشریف", "Palestine", "فلسطین", 31.7683, 35.2137, "Asia/Jerusalem"),

        // South Asia & Southeast Asia
        CityInfo("bd_dha", "Dhaka", "ڈھاکہ", "Bangladesh", "بنگلہ دیش", 23.8103, 90.4125, "Asia/Dhaka"),
        CityInfo("in_del", "Delhi", "دہلی", "India", "بھارت", 28.7041, 77.1025, "Asia/Kolkata"),
        CityInfo("in_bom", "Mumbai", "ممبئی", "India", "بھارت", 19.0760, 72.8777, "Asia/Kolkata"),
        CityInfo("in_hyd", "Hyderabad (Deccan)", "حیدرآباد دکن", "India", "بھارت", 17.3850, 78.4867, "Asia/Kolkata"),
        CityInfo("my_kul", "Kuala Lumpur", "کوالالمپور", "Malaysia", "ملائیشیا", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityInfo("id_jkt", "Jakarta", "جکارتہ", "Indonesia", "انڈونیشیا", -6.2088, 106.8456, "Asia/Jakarta"),

        // Europe, Americas & Australia
        CityInfo("gb_lon", "London", "لندن", "United Kingdom", "برطانیہ", 51.5074, -0.1278, "Europe/London"),
        CityInfo("gb_bhx", "Birmingham", "برمنگھم", "United Kingdom", "برطانیہ", 52.4862, -1.8904, "Europe/London"),
        CityInfo("gb_man", "Manchester", "مانچسٹر", "United Kingdom", "برطانیہ", 53.4808, -2.2426, "Europe/London"),
        CityInfo("us_nyc", "New York", "نیویارک", "United States", "امریکہ", 40.7128, -74.0060, "America/New_York"),
        CityInfo("us_chi", "Chicago", "شکاگو", "United States", "امریکہ", 41.8781, -87.6298, "America/Chicago"),
        CityInfo("us_hou", "Houston", "ہیوسٹن", "United States", "امریکہ", 29.7604, -95.3698, "America/Chicago"),
        CityInfo("us_lax", "Los Angeles", "لاس اینجلس", "United States", "امریکہ", 34.0522, -118.2437, "America/Los_Angeles"),
        CityInfo("ca_tor", "Toronto", "ٹورنٹو", "Canada", "کینیڈا", 43.6532, -79.3832, "America/Toronto"),
        CityInfo("au_syd", "Sydney", "سڈنی", "Australia", "آسٹریلیا", -33.8688, 151.2093, "Australia/Sydney")
    )

    val DEFAULT_CITY = CITIES.first { it.id == "sa_mak" } // Makkah default

    fun findCityById(id: String): CityInfo? {
        return CITIES.find { it.id == id }
    }
}

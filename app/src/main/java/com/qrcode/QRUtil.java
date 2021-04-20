package com.qrcode;

import java.io.UnsupportedEncodingException;

/**
 * QRUtil
 * @author Kazuhiko Arase
 */
class QRUtil {

    private QRUtil() {
    }

    public static String getJISEncoding() {
        return "SJIS";
    }

    public static int[] getPatternPosition(int typeNumber) {
        return PATTERN_POSITION_TABLE[typeNumber - 1];
    }

    private static final int[][] PATTERN_POSITION_TABLE ={
            {},
            {6, 18},
            {6, 22},
            {6, 26},
            {6, 30},
            {6, 34},
            {6, 22, 38},
            {6, 24, 42},
            {6, 26, 46},
            {6, 28, 50},
            {6, 30, 54},
            {6, 32, 58},
            {6, 34, 62},
            {6, 26, 46, 66},
            {6, 26, 48, 70},
            {6, 26, 50, 74},
            {6, 30, 54, 78},
            {6, 30, 56, 82},
            {6, 30, 58, 86},
            {6, 34, 62, 90},
            {6, 28, 50, 72, 94},
            {6, 26, 50, 74, 98},
            {6, 30, 54, 78, 102},
            {6, 28, 54, 80, 106},
            {6, 32, 58, 84, 110},
            {6, 30, 58, 86, 114},
            {6, 34, 62, 90, 118},
            {6, 26, 50, 74, 98, 122},
            {6, 30, 54, 78, 102, 126},
            {6, 26, 52, 78, 104, 130},
            {6, 30, 56, 82, 108, 134},
            {6, 34, 60, 86, 112, 138},
            {6, 30, 58, 86, 114, 142},
            {6, 34, 62, 90, 118, 146},
            {6, 30, 54, 78, 102, 126, 150},
            {6, 24, 50, 76, 102, 128, 154},
            {6, 28, 54, 80, 106, 132, 158},
            {6, 32, 58, 84, 110, 136, 162},
            {6, 26, 54, 82, 110, 138, 166},
            {6, 30, 58, 86, 114, 142, 170}
    };

    /*private static int[][][] MAX_LENGTH = {
            { {41,  25,  17,  10},  {34,  20,  14,  8},   {27,  16,  11,  7},  {17,  10,  7,   4} },
            { {77,  47,  32,  20},  {63,  38,  26,  16},  {48,  29,  20,  12}, {34,  20,  14,  8} },
            { {127, 77,  53,  32},  {101, 61,  42,  26},  {77,  47,  32,  20}, {58,  35,  24,  15} },
            { {187, 114, 78,  48},  {149, 90,  62,  38},  {111, 67,  46,  28}, {82,  50,  34,  21} },
            { {255, 154, 106, 65},  {202, 122, 84,  52},  {144, 87,  60,  37}, {106, 64,  44,  27} },
            { {322, 195, 134, 82},  {255, 154, 106, 65},  {178, 108, 74,  45}, {139, 84,  58,  36} },
            { {370, 224, 154, 95},  {293, 178, 122, 75},  {207, 125, 86,  53}, {154, 93,  64,  39} },
            { {461, 279, 192, 118}, {365, 221, 152, 93},  {259, 157, 108, 66}, {202, 122, 84,  52} },
            { {552, 335, 230, 141}, {432, 262, 180, 111}, {312, 189, 130, 80}, {235, 143, 98,  60} },
            { {652, 395, 271, 167}, {513, 311, 213, 131}, {364, 221, 151, 93}, {288, 174, 119, 74} }
    };*/

    // each typeNumber in a different line, from 1 to 40
    // each mode in a different bracket {MODE_NUMBER}, {MODE_ALPHA_NUM}, {MODE_8BIT_BYTE}, {MODE_KANJI}
    // errorCorrectionLevel {Low, Medium, extraQuality, High}

    private static int[][][] MAX_LENGTH = {
            { {  41,   34,   27,   17}, {  25,   20,   16,   10}, {  17,   14,   11,    7}, {  10,    8,    7,    4} },
            { {  77,   63,   48,   34}, {  47,   38,   29,   20}, {  32,   26,   20,   14}, {  20,   16,   12,    8} },
            { { 127,  101,   77,   58}, {  77,   61,   47,   35}, {  53,   42,   32,   24}, {  32,   26,   20,   15} },
            { { 187,  149,  111,   82}, { 114,   90,   67,   50}, {  78,   62,   46,   34}, {  48,   38,   28,   21} },
            { { 255,  202,  144,  106}, { 154,  122,   87,   64}, { 106,   84,   60,   44}, {  65,   52,   37,   27} },
            { { 322,  255,  178,  139}, { 195,  154,  108,   84}, { 134,  106,   74,   58}, {  82,   65,   45,   36} },
            { { 370,  293,  207,  154}, { 224,  178,  125,   93}, { 154,  122,   86,   64}, {  95,   75,   53,   39} },
            { { 461,  365,  259,  202}, { 279,  221,  157,  122}, { 192,  152,  108,   84}, { 118,   93,   66,   52} },
            { { 552,  432,  312,  235}, { 335,  262,  189,  143}, { 230,  180,  130,   98}, { 141,  111,   80,   60} },
            { { 652,  513,  364,  288}, { 395,  311,  221,  174}, { 271,  213,  151,  119}, { 167,  131,   93,   74} },
            { { 772,  604,  427,  331}, { 468,  366,  259,  200}, { 321,  251,  177,  137}, { 198,  155,  109,   85} },
            { { 883,  691,  489,  374}, { 535,  419,  296,  227}, { 367,  287,  203,  155}, { 226,  177,  125,   96} },
            { {1022,  796,  580,  427}, { 619,  483,  352,  259}, { 425,  331,  241,  177}, { 262,  204,  149,  109} },
            { {1101,  871,  621,  468}, { 667,  528,  376,  283}, { 458,  362,  258,  194}, { 282,  223,  159,  120} },
            { {1250,  991,  703,  530}, { 758,  600,  426,  321}, { 520,  412,  292,  220}, { 320,  254,  180,  136} },
            { {1408, 1082,  775,  602}, { 854,  656,  470,  365}, { 586,  450,  322,  250}, { 361,  277,  198,  154} },
            { {1548, 1212,  876,  674}, { 938,  734,  531,  408}, { 644,  504,  364,  280}, { 397,  310,  224,  173} },
            { {1725, 1346,  948,  746}, {1046,  816,  574,  452}, { 718,  560,  394,  310}, { 442,  345,  243,  191} },
            { {1903, 1500, 1063,  813}, {1153,  909,  644,  493}, { 792,  624,  442,  338}, { 488,  384,  272,  208} },
            { {2061, 1600, 1159,  919}, {1249,  970,  702,  557}, { 858,  666,  482,  382}, { 528,  410,  297,  235} },
            { {2232, 1708, 1224,  969}, {1352, 1035,  742,  587}, { 929,  711,  509,  403}, { 572,  438,  314,  248} },
            { {2409, 1872, 1358, 1056}, {1460, 1134,  823,  640}, {1003,  779,  565,  439}, { 618,  480,  348,  270} },
            { {2620, 2059, 1468, 1108}, {1588, 1248,  890,  672}, {1091,  857,  611,  461}, { 672,  528,  376,  284} },
            { {2812, 2188, 1588, 1228}, {1704, 1326,  963,  744}, {1171,  911,  661,  511}, { 721,  561,  407,  315} },
            { {3057, 2395, 1718, 1286}, {1853, 1451, 1041,  779}, {1273,  997,  715,  535}, { 784,  614,  440,  330} },
            { {3283, 2544, 1804, 1425}, {1990, 1542, 1094,  864}, {1367, 1059,  751,  593}, { 842,  652,  462,  365} },
            { {3517, 2701, 1933, 1501}, {2132, 1637, 1172,  910}, {1465, 1125,  805,  625}, { 902,  692,  496,  385} },
            { {3669, 2857, 2085, 1581}, {2223, 1732, 1263,  958}, {1528, 1190,  868,  658}, { 940,  732,  534,  405} },
            { {3909, 3035, 2181, 1677}, {2369, 1839, 1322, 1016}, {1628, 1264,  908,  698}, {1002,  778,  559,  430} },
            { {4158, 3289, 2358, 1782}, {2520, 1994, 1429, 1080}, {1732, 1370,  982,  742}, {1066,  843,  604,  457} },
            { {4417, 3486, 2473, 1897}, {2677, 2113, 1499, 1150}, {1840, 1452, 1030,  790}, {1132,  894,  634,  486} },
            { {4686, 3693, 2670, 2022}, {2840, 2238, 1618, 1226}, {1952, 1538, 1112,  842}, {1201,  947,  684,  518} },
            { {4965, 3909, 2805, 2157}, {3009, 2369, 1700, 1307}, {2068, 1628, 1168,  898}, {1273, 1002,  719,  553} },
            { {5253, 4134, 2949, 2301}, {3183, 2506, 1787, 1394}, {2188, 1722, 1228,  958}, {1347, 1060,  756,  590} },
            { {5529, 4343, 3081, 2361}, {3351, 2632, 1867, 1431}, {2303, 1809, 1283,  983}, {1417, 1113,  790,  605} },
            { {5836, 4588, 3244, 2524}, {3537, 2780, 1966, 1530}, {2431, 1911, 1351, 1051}, {1496, 1176,  832,  647} },
            { {6153, 4775, 3417, 2625}, {3729, 2894, 2071, 1591}, {2563, 1989, 1423, 1093}, {1577, 1224,  876,  673} },
            { {6479, 5039, 3599, 2735}, {3927, 3054, 2181, 1658}, {2699, 2099, 1499, 1139}, {1661, 1292,  923,  701} },
            { {6743, 5313, 3791, 2927}, {4087, 3220, 2298, 1774}, {2809, 2213, 1579, 1219}, {1729, 1362,  972,  750} },
            { {7089, 5596, 3993, 3057}, {4296, 3391, 2420, 1852}, {2953, 2331, 1663, 1273}, {1817, 1435, 1024,  784} },
    };

    public static int getMaxTypeNumber(){
        return MAX_LENGTH.length;
    }

    public static int getMaxLength(int mode, int errorCorrectionLevel, int typeNumber) {

        int t = typeNumber - 1;
        int e = 0;
        int m = 0;

        switch(errorCorrectionLevel) {
            case ErrorCorrectionLevel.L : e = 0; break;
            case ErrorCorrectionLevel.M : e = 1; break;
            case ErrorCorrectionLevel.Q : e = 2; break;
            case ErrorCorrectionLevel.H : e = 3; break;
            default :
                throw new IllegalArgumentException("e:" + errorCorrectionLevel);
        }

        switch(mode) {
            case Mode.MODE_NUMBER    : m = 0; break;
            case Mode.MODE_ALPHA_NUM : m = 1; break;
            case Mode.MODE_8BIT_BYTE : m = 2; break;
            case Mode.MODE_KANJI     : m = 3; break;
            default :
                throw new IllegalArgumentException("m:" + mode);
        }

        return MAX_LENGTH[t][m][e];
    }

    /**
     * エラー訂正多項式を取得する。
     */
    public static Polynomial getErrorCorrectPolynomial(int errorCorrectLength) {

        Polynomial a = new Polynomial(new int[]{1});

        for (int i = 0; i < errorCorrectLength; i++) {
            a = a.multiply(new Polynomial(new int[]{1, QRMath.gexp(i) }) );
        }

        return a;
    }

    /**
     * 指定されたパターンのマスクを取得する。
     */
    public static boolean getMask(int maskPattern, int i, int j) {

        switch (maskPattern) {

            case MaskPattern.PATTERN000 : return (i + j) % 2 == 0;
            case MaskPattern.PATTERN001 : return i % 2 == 0;
            case MaskPattern.PATTERN010 : return j % 3 == 0;
            case MaskPattern.PATTERN011 : return (i + j) % 3 == 0;
            case MaskPattern.PATTERN100 : return (i / 2 + j / 3) % 2 == 0;
            case MaskPattern.PATTERN101 : return (i * j) % 2 + (i * j) % 3 == 0;
            case MaskPattern.PATTERN110 : return ( (i * j) % 2 + (i * j) % 3) % 2 == 0;
            case MaskPattern.PATTERN111 : return ( (i * j) % 3 + (i + j) % 2) % 2 == 0;

            default :
                throw new IllegalArgumentException("mask:" + maskPattern);
        }
    }

    /**
     * 失点を取得する
     */
    public static int getLostPoint(QRCode qrCode) {

        int moduleCount = qrCode.getModuleCount();

        int lostPoint = 0;


        // LEVEL1

        for (int row = 0; row < moduleCount; row++) {

            for (int col = 0; col < moduleCount; col++) {

                int sameCount = 0;
                boolean dark = qrCode.isDark(row, col);

                for (int r = -1; r <= 1; r++) {

                    if (row + r < 0 || moduleCount <= row + r) {
                        continue;
                    }

                    for (int c = -1; c <= 1; c++) {

                        if (col + c < 0 || moduleCount <= col + c) {
                            continue;
                        }

                        if (r == 0 && c == 0) {
                            continue;
                        }

                        if (dark == qrCode.isDark(row + r, col + c) ) {
                            sameCount++;
                        }
                    }
                }

                if (sameCount > 5) {
                    lostPoint += (3 + sameCount - 5);
                }
            }
        }

        // LEVEL2

        for (int row = 0; row < moduleCount - 1; row++) {
            for (int col = 0; col < moduleCount - 1; col++) {
                int count = 0;
                if (qrCode.isDark(row,     col    ) ) count++;
                if (qrCode.isDark(row + 1, col    ) ) count++;
                if (qrCode.isDark(row,     col + 1) ) count++;
                if (qrCode.isDark(row + 1, col + 1) ) count++;
                if (count == 0 || count == 4) {
                    lostPoint += 3;
                }
            }
        }

        // LEVEL3

        for (int row = 0; row < moduleCount; row++) {
            for (int col = 0; col < moduleCount - 6; col++) {
                if (qrCode.isDark(row, col)
                        && !qrCode.isDark(row, col + 1)
                        &&  qrCode.isDark(row, col + 2)
                        &&  qrCode.isDark(row, col + 3)
                        &&  qrCode.isDark(row, col + 4)
                        && !qrCode.isDark(row, col + 5)
                        &&  qrCode.isDark(row, col + 6) ) {
                    lostPoint += 40;
                }
            }
        }

        for (int col = 0; col < moduleCount; col++) {
            for (int row = 0; row < moduleCount - 6; row++) {
                if (qrCode.isDark(row, col)
                        && !qrCode.isDark(row + 1, col)
                        &&  qrCode.isDark(row + 2, col)
                        &&  qrCode.isDark(row + 3, col)
                        &&  qrCode.isDark(row + 4, col)
                        && !qrCode.isDark(row + 5, col)
                        &&  qrCode.isDark(row + 6, col) ) {
                    lostPoint += 40;
                }
            }
        }

        // LEVEL4

        int darkCount = 0;

        for (int col = 0; col < moduleCount; col++) {
            for (int row = 0; row < moduleCount; row++) {
                if (qrCode.isDark(row, col) ) {
                    darkCount++;
                }
            }
        }

        int ratio = Math.abs(100 * darkCount / moduleCount / moduleCount - 50) / 5;
        lostPoint += ratio * 10;

        return lostPoint;
    }

    public static int getMode(String s) {
        if (isAlphaNum(s) ) {
            if (isNumber(s) ) {
                return Mode.MODE_NUMBER;
            }
            return Mode.MODE_ALPHA_NUM;
        } else if (isKanji(s) ) {
            return Mode.MODE_KANJI;
        } else {
            return Mode.MODE_8BIT_BYTE;
        }
    }

    private static boolean isNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!('0' <= c && c <= '9') ) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAlphaNum(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!('0' <= c && c <= '9') && !('A' <= c && c <= 'Z') && " $%*+-./:".indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isKanji(String s) {

        try {

            byte[] data = s.getBytes(QRUtil.getJISEncoding() );

            int i = 0;

            while (i + 1 < data.length) {

                int c = ( (0xff & data[i]) << 8) | (0xff & data[i + 1]);

                if (!(0x8140 <= c && c <= 0x9FFC) && !(0xE040 <= c && c <= 0xEBBF) ) {
                    return false;
                }

                i += 2;
            }

            if (i < data.length) {
                return false;
            }

            return true;

        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage() );
        }
    }

    private static final int G15 = (1 << 10) | (1 << 8) | (1 << 5)
            | (1 << 4) | (1 << 2) | (1 << 1) | (1 << 0);

    private static final int G18 = (1 << 12) | (1 << 11) | (1 << 10)
            | (1 << 9) | (1 << 8) | (1 << 5) | (1 << 2) | (1 << 0);

    private static final int G15_MASK = (1 << 14) | (1 << 12) | (1 << 10)
            | (1 << 4) | (1 << 1);

    public static int getBCHTypeInfo(int data) {
        int d = data << 10;
        while (getBCHDigit(d) - getBCHDigit(G15) >= 0) {
            d ^= (G15 << (getBCHDigit(d) - getBCHDigit(G15) ) );
        }
        return ( (data << 10) | d) ^ G15_MASK;
    }

    public static int getBCHTypeNumber(int data) {
        int d = data << 12;
        while (getBCHDigit(d) - getBCHDigit(G18) >= 0) {
            d ^= (G18 << (getBCHDigit(d) - getBCHDigit(G18) ) );
        }
        return (data << 12) | d;
    }

    private static int getBCHDigit(int data) {

        int digit = 0;

        while (data != 0) {
            digit++;
            data >>>= 1;
        }

        return digit;

    }
}
package eu.kanade.tachiyomi.extension.zh.guoman8


import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.regex.Pattern
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class Guoman8 : HttpSource() {

    override val name = "奇漫屋"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private val htmlUrl = "http://www.qiman6.com"

    private val numMap = mapOf<String, Int>(
        "0" to 0, "1" to 1, "2" to 2, "3" to 3, "4" to 4, "5" to 5, "6" to 6, "7" to 7, "8" to 8, "9" to 9, "a" to 10, "b" to 11, "c" to 12, "d" to 13, "e" to 14, "f" to 15, "g" to 16, "h" to 17, "i" to 18, "j" to 19, "k" to 20, "l" to 21, "m" to 22, "n" to 23, "o" to 24, "p" to 25, "q" to 26, "r" to 27, "s" to 28, "t" to 29, "u" to 30, "v" to 31, "w" to 32, "x" to 33, "y" to 34, "z" to 35, "A" to 36, "B" to 37, "C" to 38, "D" to 39, "E" to 40, "F" to 41, "G" to 42, "H" to 43, "I" to 44, "J" to 45, "K" to 46, "L" to 47, "M" to 48, "N" to 49,
        "O" to 50, "P" to 51, "Q" to 52, "R" to 53, "S" to 54, "T" to 55, "U" to 56, "V" to 57, "W" to 58, "X" to 59, "Y" to 60, "Z" to 61, "10" to 62, "11" to 63, "12" to 64, "13" to 65, "14" to 66, "15" to 67, "16" to 68, "17" to 69, "18" to 70, "19" to 71, "1a" to 72, "1b" to 73, "1c" to 74, "1d" to 75, "1e" to 76, "1f" to 77, "1g" to 78, "1h" to 79, "1i" to 80, "1j" to 81, "1k" to 82, "1l" to 83, "1m" to 84, "1n" to 85, "1o" to 86, "1p" to 87, "1q" to 88, "1r" to 89, "1s" to 90, "1t" to 91, "1u" to 92, "1v" to 93, "1w" to 94, "1x" to 95, "1y" to 96, "1z" to 97, "1A" to 98, "1B" to 99,
        "1C" to 100, "1D" to 101, "1E" to 102, "1F" to 103, "1G" to 104, "1H" to 105, "1I" to 106, "1J" to 107, "1K" to 108, "1L" to 109, "1M" to 110, "1N" to 111, "1O" to 112, "1P" to 113, "1Q" to 114, "1R" to 115, "1S" to 116, "1T" to 117, "1U" to 118, "1V" to 119, "1W" to 120, "1X" to 121, "1Y" to 122, "1Z" to 123, "20" to 124, "21" to 125, "22" to 126, "23" to 127, "24" to 128, "25" to 129, "26" to 130, "27" to 131, "28" to 132, "29" to 133, "2a" to 134, "2b" to 135, "2c" to 136, "2d" to 137, "2e" to 138, "2f" to 139, "2g" to 140, "2h" to 141, "2i" to 142, "2j" to 143, "2k" to 144, "2l" to 145, "2m" to 146, "2n" to 147, "2o" to 148, "2p" to 149,
        "2q" to 150, "2r" to 151, "2s" to 152, "2t" to 153, "2u" to 154, "2v" to 155, "2w" to 156, "2x" to 157, "2y" to 158, "2z" to 159, "2A" to 160, "2B" to 161, "2C" to 162, "2D" to 163, "2E" to 164, "2F" to 165, "2G" to 166, "2H" to 167, "2I" to 168, "2J" to 169, "2K" to 170, "2L" to 171, "2M" to 172, "2N" to 173, "2O" to 174, "2P" to 175, "2Q" to 176, "2R" to 177, "2S" to 178, "2T" to 179, "2U" to 180, "2V" to 181, "2W" to 182, "2X" to 183, "2Y" to 184, "2Z" to 185, "30" to 186, "31" to 187, "32" to 188, "33" to 189, "34" to 190, "35" to 191, "36" to 192, "37" to 193, "38" to 194, "39" to 195, "3a" to 196, "3b" to 197, "3c" to 198, "3d" to 199,
        "3e" to 200, "3f" to 201, "3g" to 202, "3h" to 203, "3i" to 204, "3j" to 205, "3k" to 206, "3l" to 207, "3m" to 208, "3n" to 209, "3o" to 210, "3p" to 211, "3q" to 212, "3r" to 213, "3s" to 214, "3t" to 215, "3u" to 216, "3v" to 217, "3w" to 218, "3x" to 219, "3y" to 220, "3z" to 221, "3A" to 222, "3B" to 223, "3C" to 224, "3D" to 225, "3E" to 226, "3F" to 227, "3G" to 228, "3H" to 229, "3I" to 230, "3J" to 231, "3K" to 232, "3L" to 233, "3M" to 234, "3N" to 235, "3O" to 236, "3P" to 237, "3Q" to 238, "3R" to 239, "3S" to 240, "3T" to 241, "3U" to 242, "3V" to 243, "3W" to 244, "3X" to 245, "3Y" to 246, "3Z" to 247, "40" to 248, "41" to 249,
        "42" to 250, "43" to 251, "44" to 252, "45" to 253, "46" to 254, "47" to 255, "48" to 256, "49" to 257, "4a" to 258, "4b" to 259, "4c" to 260, "4d" to 261, "4e" to 262, "4f" to 263, "4g" to 264, "4h" to 265, "4i" to 266, "4j" to 267, "4k" to 268, "4l" to 269, "4m" to 270, "4n" to 271, "4o" to 272, "4p" to 273, "4q" to 274, "4r" to 275, "4s" to 276, "4t" to 277, "4u" to 278, "4v" to 279, "4w" to 280, "4x" to 281, "4y" to 282, "4z" to 283, "4A" to 284, "4B" to 285, "4C" to 286, "4D" to 287, "4E" to 288, "4F" to 289, "4G" to 290, "4H" to 291, "4I" to 292, "4J" to 293, "4K" to 294, "4L" to 295, "4M" to 296, "4N" to 297, "4O" to 298, "4P" to 299,
        "4Q" to 300, "4R" to 301, "4S" to 302, "4T" to 303, "4U" to 304, "4V" to 305, "4W" to 306, "4X" to 307, "4Y" to 308, "4Z" to 309, "50" to 310, "51" to 311, "52" to 312, "53" to 313, "54" to 314, "55" to 315, "56" to 316, "57" to 317, "58" to 318, "59" to 319, "5a" to 320, "5b" to 321, "5c" to 322, "5d" to 323, "5e" to 324, "5f" to 325, "5g" to 326, "5h" to 327, "5i" to 328, "5j" to 329, "5k" to 330, "5l" to 331, "5m" to 332, "5n" to 333, "5o" to 334, "5p" to 335, "5q" to 336, "5r" to 337, "5s" to 338, "5t" to 339, "5u" to 340, "5v" to 341, "5w" to 342, "5x" to 343, "5y" to 344, "5z" to 345, "5A" to 346, "5B" to 347, "5C" to 348, "5D" to 349,
        "5E" to 350, "5F" to 351, "5G" to 352, "5H" to 353, "5I" to 354, "5J" to 355, "5K" to 356, "5L" to 357, "5M" to 358, "5N" to 359, "5O" to 360, "5P" to 361, "5Q" to 362, "5R" to 363, "5S" to 364, "5T" to 365, "5U" to 366, "5V" to 367, "5W" to 368, "5X" to 369, "5Y" to 370, "5Z" to 371, "60" to 372, "61" to 373, "62" to 374, "63" to 375, "64" to 376, "65" to 377, "66" to 378, "67" to 379, "68" to 380, "69" to 381, "6a" to 382, "6b" to 383, "6c" to 384, "6d" to 385, "6e" to 386, "6f" to 387, "6g" to 388, "6h" to 389, "6i" to 390, "6j" to 391, "6k" to 392, "6l" to 393, "6m" to 394, "6n" to 395, "6o" to 396, "6p" to 397, "6q" to 398, "6r" to 399,
        "6s" to 400, "6t" to 401, "6u" to 402, "6v" to 403, "6w" to 404, "6x" to 405, "6y" to 406, "6z" to 407, "6A" to 408, "6B" to 409, "6C" to 410, "6D" to 411, "6E" to 412, "6F" to 413, "6G" to 414, "6H" to 415, "6I" to 416, "6J" to 417, "6K" to 418, "6L" to 419, "6M" to 420, "6N" to 421, "6O" to 422, "6P" to 423, "6Q" to 424, "6R" to 425, "6S" to 426, "6T" to 427, "6U" to 428, "6V" to 429, "6W" to 430, "6X" to 431, "6Y" to 432, "6Z" to 433, "70" to 434, "71" to 435, "72" to 436, "73" to 437, "74" to 438, "75" to 439, "76" to 440, "77" to 441, "78" to 442, "79" to 443, "7a" to 444, "7b" to 445, "7c" to 446, "7d" to 447, "7e" to 448, "7f" to 449,
        "7g" to 450, "7h" to 451, "7i" to 452, "7j" to 453, "7k" to 454, "7l" to 455, "7m" to 456, "7n" to 457, "7o" to 458, "7p" to 459, "7q" to 460, "7r" to 461, "7s" to 462, "7t" to 463, "7u" to 464, "7v" to 465, "7w" to 466, "7x" to 467, "7y" to 468, "7z" to 469, "7A" to 470, "7B" to 471, "7C" to 472, "7D" to 473, "7E" to 474, "7F" to 475, "7G" to 476, "7H" to 477, "7I" to 478, "7J" to 479, "7K" to 480, "7L" to 481, "7M" to 482, "7N" to 483, "7O" to 484, "7P" to 485, "7Q" to 486, "7R" to 487, "7S" to 488, "7T" to 489, "7U" to 490, "7V" to 491, "7W" to 492, "7X" to 493, "7Y" to 494, "7Z" to 495, "80" to 496, "81" to 497, "82" to 498, "83" to 499,
        "84" to 500, "85" to 501, "86" to 502, "87" to 503, "88" to 504, "89" to 505, "8a" to 506, "8b" to 507, "8c" to 508, "8d" to 509, "8e" to 510, "8f" to 511, "8g" to 512, "8h" to 513, "8i" to 514, "8j" to 515, "8k" to 516, "8l" to 517, "8m" to 518, "8n" to 519, "8o" to 520, "8p" to 521, "8q" to 522, "8r" to 523, "8s" to 524, "8t" to 525, "8u" to 526, "8v" to 527, "8w" to 528, "8x" to 529, "8y" to 530, "8z" to 531, "8A" to 532, "8B" to 533, "8C" to 534, "8D" to 535, "8E" to 536, "8F" to 537, "8G" to 538, "8H" to 539, "8I" to 540, "8J" to 541, "8K" to 542, "8L" to 543, "8M" to 544, "8N" to 545, "8O" to 546, "8P" to 547, "8Q" to 548, "8R" to 549,
        "8S" to 550, "8T" to 551, "8U" to 552, "8V" to 553, "8W" to 554, "8X" to 555, "8Y" to 556, "8Z" to 557, "90" to 558, "91" to 559, "92" to 560, "93" to 561, "94" to 562, "95" to 563, "96" to 564, "97" to 565, "98" to 566, "99" to 567, "9a" to 568, "9b" to 569, "9c" to 570, "9d" to 571, "9e" to 572, "9f" to 573, "9g" to 574, "9h" to 575, "9i" to 576, "9j" to 577, "9k" to 578, "9l" to 579, "9m" to 580, "9n" to 581, "9o" to 582, "9p" to 583, "9q" to 584, "9r" to 585, "9s" to 586, "9t" to 587, "9u" to 588, "9v" to 589, "9w" to 590, "9x" to 591, "9y" to 592, "9z" to 593, "9A" to 594, "9B" to 595, "9C" to 596, "9D" to 597, "9E" to 598, "9F" to 599,
        "9G" to 600, "9H" to 601, "9I" to 602, "9J" to 603, "9K" to 604, "9L" to 605, "9M" to 606, "9N" to 607, "9O" to 608, "9P" to 609, "9Q" to 610, "9R" to 611, "9S" to 612, "9T" to 613, "9U" to 614, "9V" to 615, "9W" to 616, "9X" to 617, "9Y" to 618, "9Z" to 619, "a0" to 620, "a1" to 621, "a2" to 622, "a3" to 623, "a4" to 624, "a5" to 625, "a6" to 626, "a7" to 627, "a8" to 628, "a9" to 629, "aa" to 630, "ab" to 631, "ac" to 632, "ad" to 633, "ae" to 634, "af" to 635, "ag" to 636, "ah" to 637, "ai" to 638, "aj" to 639, "ak" to 640, "al" to 641, "am" to 642, "an" to 643, "ao" to 644, "ap" to 645, "aq" to 646, "ar" to 647, "as" to 648, "at" to 649,
        "au" to 650, "av" to 651, "aw" to 652, "ax" to 653, "ay" to 654, "az" to 655, "aA" to 656, "aB" to 657, "aC" to 658, "aD" to 659, "aE" to 660, "aF" to 661, "aG" to 662, "aH" to 663, "aI" to 664, "aJ" to 665, "aK" to 666, "aL" to 667, "aM" to 668, "aN" to 669, "aO" to 670, "aP" to 671, "aQ" to 672, "aR" to 673, "aS" to 674, "aT" to 675, "aU" to 676, "aV" to 677, "aW" to 678, "aX" to 679, "aY" to 680, "aZ" to 681, "b0" to 682, "b1" to 683, "b2" to 684, "b3" to 685, "b4" to 686, "b5" to 687, "b6" to 688, "b7" to 689, "b8" to 690, "b9" to 691, "ba" to 692, "bb" to 693, "bc" to 694, "bd" to 695, "be" to 696, "bf" to 697, "bg" to 698, "bh" to 699,
        "bi" to 700, "bj" to 701, "bk" to 702, "bl" to 703, "bm" to 704, "bn" to 705, "bo" to 706, "bp" to 707, "bq" to 708, "br" to 709, "bs" to 710, "bt" to 711, "bu" to 712, "bv" to 713, "bw" to 714, "bx" to 715, "by" to 716, "bz" to 717, "bA" to 718, "bB" to 719, "bC" to 720, "bD" to 721, "bE" to 722, "bF" to 723, "bG" to 724, "bH" to 725, "bI" to 726, "bJ" to 727, "bK" to 728, "bL" to 729, "bM" to 730, "bN" to 731, "bO" to 732, "bP" to 733, "bQ" to 734, "bR" to 735, "bS" to 736, "bT" to 737, "bU" to 738, "bV" to 739, "bW" to 740, "bX" to 741, "bY" to 742, "bZ" to 743, "c0" to 744, "c1" to 745, "c2" to 746, "c3" to 747, "c4" to 748, "c5" to 749,
        "c6" to 750, "c7" to 751, "c8" to 752, "c9" to 753, "ca" to 754, "cb" to 755, "cc" to 756, "cd" to 757, "ce" to 758, "cf" to 759, "cg" to 760, "ch" to 761, "ci" to 762, "cj" to 763, "ck" to 764, "cl" to 765, "cm" to 766, "cn" to 767, "co" to 768, "cp" to 769, "cq" to 770, "cr" to 771, "cs" to 772, "ct" to 773, "cu" to 774, "cv" to 775, "cw" to 776, "cx" to 777, "cy" to 778, "cz" to 779, "cA" to 780, "cB" to 781, "cC" to 782, "cD" to 783, "cE" to 784, "cF" to 785, "cG" to 786, "cH" to 787, "cI" to 788, "cJ" to 789, "cK" to 790, "cL" to 791, "cM" to 792, "cN" to 793, "cO" to 794, "cP" to 795, "cQ" to 796, "cR" to 797, "cS" to 798, "cT" to 799,
        "cU" to 800, "cV" to 801, "cW" to 802, "cX" to 803, "cY" to 804, "cZ" to 805, "d0" to 806, "d1" to 807, "d2" to 808, "d3" to 809, "d4" to 810, "d5" to 811, "d6" to 812, "d7" to 813, "d8" to 814, "d9" to 815, "da" to 816, "db" to 817, "dc" to 818, "dd" to 819, "de" to 820, "df" to 821, "dg" to 822, "dh" to 823, "di" to 824, "dj" to 825, "dk" to 826, "dl" to 827, "dm" to 828, "dn" to 829, "do" to 830, "dp" to 831, "dq" to 832, "dr" to 833, "ds" to 834, "dt" to 835, "du" to 836, "dv" to 837, "dw" to 838, "dx" to 839, "dy" to 840, "dz" to 841, "dA" to 842, "dB" to 843, "dC" to 844, "dD" to 845, "dE" to 846, "dF" to 847, "dG" to 848, "dH" to 849,
        "dI" to 850, "dJ" to 851, "dK" to 852, "dL" to 853, "dM" to 854, "dN" to 855, "dO" to 856, "dP" to 857, "dQ" to 858, "dR" to 859, "dS" to 860, "dT" to 861, "dU" to 862, "dV" to 863, "dW" to 864, "dX" to 865, "dY" to 866, "dZ" to 867, "e0" to 868, "e1" to 869, "e2" to 870, "e3" to 871, "e4" to 872, "e5" to 873, "e6" to 874, "e7" to 875, "e8" to 876, "e9" to 877, "ea" to 878, "eb" to 879, "ec" to 880, "ed" to 881, "ee" to 882, "ef" to 883, "eg" to 884, "eh" to 885, "ei" to 886, "ej" to 887, "ek" to 888, "el" to 889, "em" to 890, "en" to 891, "eo" to 892, "ep" to 893, "eq" to 894, "er" to 895, "es" to 896, "et" to 897, "eu" to 898, "ev" to 899,
        "ew" to 900, "ex" to 901, "ey" to 902, "ez" to 903, "eA" to 904, "eB" to 905, "eC" to 906, "eD" to 907, "eE" to 908, "eF" to 909, "eG" to 910, "eH" to 911, "eI" to 912, "eJ" to 913, "eK" to 914, "eL" to 915, "eM" to 916, "eN" to 917, "eO" to 918, "eP" to 919, "eQ" to 920, "eR" to 921, "eS" to 922, "eT" to 923, "eU" to 924, "eV" to 925, "eW" to 926, "eX" to 927, "eY" to 928, "eZ" to 929, "f0" to 930, "f1" to 931, "f2" to 932, "f3" to 933, "f4" to 934, "f5" to 935, "f6" to 936, "f7" to 937, "f8" to 938, "f9" to 939, "fa" to 940, "fb" to 941, "fc" to 942, "fd" to 943, "fe" to 944, "ff" to 945, "fg" to 946, "fh" to 947, "fi" to 948, "fj" to 949,
        "fk" to 950, "fl" to 951, "fm" to 952, "fn" to 953, "fo" to 954, "fp" to 955, "fq" to 956, "fr" to 957, "fs" to 958, "ft" to 959, "fu" to 960, "fv" to 961, "fw" to 962, "fx" to 963, "fy" to 964, "fz" to 965, "fA" to 966, "fB" to 967, "fC" to 968, "fD" to 969, "fE" to 970, "fF" to 971, "fG" to 972, "fH" to 973, "fI" to 974, "fJ" to 975, "fK" to 976, "fL" to 977, "fM" to 978, "fN" to 979, "fO" to 980, "fP" to 981, "fQ" to 982, "fR" to 983, "fS" to 984, "fT" to 985, "fU" to 986, "fV" to 987, "fW" to 988, "fX" to 989, "fY" to 990, "fZ" to 991, "g0" to 992, "g1" to 993, "g2" to 994, "g3" to 995, "g4" to 996, "g5" to 997, "g6" to 998, "g7" to 999,
        "g8" to 1000)

    private fun myGet(url: String) = GET(url, headers)

    override fun popularMangaRequest(page: Int): Request {
        return myGet("${htmlUrl}/rank/1-$page.html")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return myGet("${htmlUrl}/rank/5-$page.html")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.comicInfo div.info h1").text()
        thumbnail_url = document.select("div.comicInfo div.cover div.img img").attr("src")
        author = getMangeInfo(document.select("div.comicInfo div.ib.info p span.ib.l"), "作者")
        artist = "Tachiyomi:ZongerZY"
        genre = getMangeInfo(document.select("div.comicInfo div.ib.info p.gray span.ib.l"), "类别")
        status = if (document.select("div.comicInfo div.ib.info p.gray span.ib.s").get(0).text().contains("完结")) 2 else 1
        description = document.select("div.comicInfo div.ib.info p.content").text()
    }

    private fun getMangeInfo(elements: Elements, info: String): String {
        for (element in elements) {
            var spanStr = Pattern.compile("\\s*|\t|\r|\n").matcher(element.text()).replaceAll("")
            if (spanStr.contains(info))
                return spanStr.split("：")[1]
        }
        return ""
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val body = response.body()!!.string()

        val requestUrl = response.request().url().toString()

        val mangaId = Regex("""/(\d+)""").find(requestUrl)!!.value.replace("/", "")

        var scapterJson = Jsoup.connect("${htmlUrl}/bookchapter/").data("id", mangaId).data("id2", "1").post().body().text().toString()

        var chapterList = ArrayList<SChapter>()

        var elements = Jsoup.parseBodyFragment(body).select("#chapter-list1").select("a.ib")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.text().trim()
                url = "${htmlUrl}" + element.attr("href")
            })
        }

        var jsonArr = JSONArray(scapterJson)
        for (i in 0 until jsonArr.length()) {
            var jsonObj = jsonArr.getJSONObject(i)
            chapterList.add(SChapter.create().apply {
                name = jsonObj.getString("chaptername")
                url = "${htmlUrl}/$mangaId/${jsonObj.getString("chapterid")}.html"
            })
        }

        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        val javascript = Pattern.compile("\\s*|\t|\r|\n").matcher(Regex("""eval(.*?)\.split""").find(body)!!.value).replaceAll("")
        val imageRule = Regex("""return(\s*)[a-zA-Z]+\}\(\'\w+(.*?)\.split""").find(javascript)!!.value
        val imageSplicRule = Regex("""\[(.*?)\]""").find(imageRule)!!.value.replace("\'", "\"")

        val imageDataRegion = Regex("""\,\'(.*?)\'""").find(imageRule)!!.value

        val imageData = Regex("""\'(.*?)\'""").find(imageDataRegion)!!.value.replace("\'", "").split("|")

        val jsonArr = JSONArray(imageSplicRule)

        var arrList = ArrayList<Page>()
        for (i in 0 until jsonArr.length()) {
            var jsonObj = jsonArr.getString(i)
            try {
                arrList.add(Page(i, "", getImageUrl(jsonObj, imageData)))
            } catch (e: Exception) {
                throw UnsupportedOperationException("{$i : " + jsonObj + "}解析失败")
            }
        }

        return arrList
    }

    private fun getImageUrl(baseUrl: String, imageData: List<String>): String {
        var url = Regex("""\w+""").replace(baseUrl, { if (imageData.get(numMap[it.value]!!).equals("")) it.value else imageData.get(numMap[it.value]!!) })

        if (url.contains("http"))
            return url
        else {
            if (url.contains(":")) {
                return "https" + url
            } else {
                return "https" + ":" + url
            }
        }
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        val responseUrl = response.request().url().toString()
        var mangasElements = document.select("div.bookList_3 div.item")
        var mangas = ArrayList<SManga>()
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("p.title a").text()
                thumbnail_url = mangaElement.select("div.book img.cover").attr("src")
                url = "${htmlUrl}" + mangaElement.select("p.title a").attr("href")
            })
        }
        if (responseUrl.contains("search.php")) {
            return MangasPage(mangas, false)
        } else {
            return MangasPage(mangas, responseUrl.split("-")[1].split(".html")[0].toInt() != 10)
        }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("${htmlUrl}/search.php?keyword=$query")
        } else {
            var params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return myGet("${htmlUrl}$params-$page.html")
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("人气榜", "/rank/1"),
        Pair("周读榜", "/rank/2"),
        Pair("月读榜", "/rank/3"),
        Pair("火爆榜", "/rank/4"),
        Pair("更新榜", "/rank/5"),
        Pair("新慢榜", "/rank/6"),
        Pair("冒险热血", "/sort/1"),
        Pair("武侠格斗", "/sort/2"),
        Pair("玄幻科幻", "/sort/3"),
        Pair("侦探推理", "/sort/4"),
        Pair("耽美爱情", "/sort/5"),
        Pair("生活漫画", "/sort/6"),
        Pair("其他漫画", "/sort/0"),
        Pair("推荐漫画", "/sort/11"),
        Pair("完结漫画", "/sort/12"),
        Pair("连载漫画", "/sort/13")
    ))

    private open class UriPartFilter(
        displayName: String,
        val vals: Array<Pair<String, String>>,
        defaultValue: Int = 0
    ) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray(), defaultValue) {
        open fun toUriPart() = vals[state].second
    }
}

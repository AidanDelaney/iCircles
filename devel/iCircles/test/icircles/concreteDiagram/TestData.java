package icircles.concreteDiagram;

import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

public class TestData {

    // TODO : include label placement into diagram checksums
    
    // possible actions
    public static final int RUN_ALL_TESTS = 0;
    public static final int RUN_TEST_LIST = 1;
    public static final int VIEW_TEST_LIST = 2;
    public static final int VIEW_ALL_TESTS = 3;
    public static final int TEST_PANEL_SIZE = 280;
    public static final boolean RANDOM_SHADING = false; // we don't have too many tests with shaded zones
    public static Font font = new Font("Helvetica", Font.BOLD,  16);
    
    public static boolean GENERATE_ALL_TEST_DATA = true; // with "run all tests", generates text for test_data array

    public static boolean TEST_EULER_THREE = false;
    
    public static boolean DO_VIEW_FAILURES = true;
    public static int TEST_DEBUG_LEVEL = 0;
    
    // settings for view-list or view-all
    public static final int VIEW_PANEL_SIZE = 100; // small panel, good for viewing multiple
    //public static final int VIEW_PANEL_SIZE = 480; // large panel, good for single, complex diagrams
    public static final int FAIL_VIEW_PANEL_SIZE = 180;
    public static final int GRID_WIDTH = 7;
    public static int[] test_list = {
        152
    };  // a set of tests of particular interest
    //public static int TASK = RUN_TEST_LIST;
    //public static int TASK = VIEW_TEST_LIST;
    //public static int TASK = RUN_ALL_TESTS;
    public static int TASK = VIEW_ALL_TESTS;

    public static double scale = 1.0; // to test scaled diagrams look= OK
    public static boolean test_journalling = false;// converts String->AbstractDescription->String
    
    public static TestDatum[] test_data = {
        /*0*/new TestDatum( "a", 80.35747263647977),
        /*1*/new TestDatum( "a b", 131.7353635695516),
        /*2*/new TestDatum( "a b c", 197.80358797941003),
        /*3*/new TestDatum( "ab", 161.405093902864),
        /*3*/new TestDatum( "ab", 161.405093902864),
        /*4*/new TestDatum( "a ab", 161.37471388954395),
        /*4*/new TestDatum( "a ab", 161.37471388954395),
        /*5*/new TestDatum( "a b ab", 151.7818962901353),
        /*6*/new TestDatum( "a b ac", 211.81155789055296),
        /*6*/new TestDatum( "a b ac", 211.81155789055296),
        /*7*/new TestDatum( "a b c ab", 221.12921720863423),
        /*8*/new TestDatum( "ab ac", 243.83530568815246),
        /*8*/new TestDatum( "ab ac", 243.83530568815246),
        /*9*/new TestDatum( "a ab ac", 243.80523568815246),
        /*9*/new TestDatum( "a ab ac", 243.80523568815246),
        /*10*/new TestDatum( "a b ab ac", 243.07882580414355),
        /*10*/new TestDatum( "a b ab ac", 243.07882580414355),
        /*11*/new TestDatum( "a b c ab ac", 234.07155278782034),
        /*12*/new TestDatum( "a bc", 211.84224793584096),
        /*12*/new TestDatum( "a bc", 211.84224793584096),
        /*13*/new TestDatum( "a ab bc", 233.95716959964912),
        /*13*/new TestDatum( "a ab bc", 233.95716959964912),
        /*14*/new TestDatum( "a b ac bc", 234.10224283310836),
        /*15*/new TestDatum( "ab ac bc", 340.787995743938),
        /*15*/new TestDatum( "ab ac bc", 340.787995743938),
        /*16*/new TestDatum( "a ab ac bc", 335.9971561294182),
        /*16*/new TestDatum( "a ab ac bc", 335.9971561294182),
        /*17*/new TestDatum( "a b ab ac bc", 340.7269258259892),
        /*17*/new TestDatum( "a b ab ac bc", 340.7269258259892),
        /*18*/new TestDatum( "a b c ab ac bc", 318.62457672968225),
        /*18*/new TestDatum( "a b c ab ac bc", 318.62457672968225),
        /*19*/new TestDatum( "abc", 259.5238409068328),
        /*19*/new TestDatum( "abc", 259.5238409068328),
        /*20*/new TestDatum( "a abc", 259.4928408615448),
        /*20*/new TestDatum( "a abc", 259.4928408615448),
        /*21*/new TestDatum( "a b abc", 248.93941627137593),
        /*21*/new TestDatum( "a b abc", 248.93941627137593),
        /*22*/new TestDatum( "a b c abc", 327.41312175282457),
        /*22*/new TestDatum( "a b c abc", 327.41312175282457),
        /*23*/new TestDatum( "ab abc", 259.46156986420885),
        /*23*/new TestDatum( "ab abc", 259.46156986420885),
        /*24*/new TestDatum( "a ab abc", 259.4311898482248),
        /*24*/new TestDatum( "a ab abc", 259.4311898482248),
        /*25*/new TestDatum( "a b ab abc", 248.87808427137594),
        /*25*/new TestDatum( "a b ab abc", 248.87808427137594),
        /*26*/new TestDatum( "a b ac abc", 316.32389685016204),
        /*26*/new TestDatum( "a b ac abc", 316.32389685016204),
        /*27*/new TestDatum( "a b c ab abc", 327.35116083487577),
        /*27*/new TestDatum( "a b c ab abc", 327.35116083487577),
        /*28*/new TestDatum( "ab ac abc", 260.9812518732166),
        /*28*/new TestDatum( "ab ac abc", 260.9812518732166),
        /*29*/new TestDatum( "a ab ac abc", 260.95118187321657),
        /*29*/new TestDatum( "a ab ac abc", 260.95118187321657),
        /*30*/new TestDatum( "a b ab ac abc", 247.17005943313438),
        /*31*/new TestDatum( "a b c ab ac abc", 344.7353256172181),
        /*31*/new TestDatum( "a b c ab ac abc", 344.7353256172181),
        /*32*/new TestDatum( "a bc abc", 316.3545868379076),
        /*32*/new TestDatum( "a bc abc", 316.3545868379076),
        /*33*/new TestDatum( "a ab bc abc", 248.03519043865083),
        /*34*/new TestDatum( "a b ac bc abc", 344.76601560496374),
        /*34*/new TestDatum( "a b ac bc abc", 344.76601560496374),
        /*35*/new TestDatum( "ab ac bc abc", 342.02093349760264),
        /*35*/new TestDatum( "ab ac bc abc", 342.02093349760264),
        /*36*/new TestDatum( "a ab ac bc abc", 352.4610746245922),
        /*36*/new TestDatum( "a ab ac bc abc", 352.4610746245922),
        /*37*/new TestDatum( "a b ab ac bc abc", 341.95986357965387),
        /*37*/new TestDatum( "a b ab ac bc abc", 341.95986357965387),
        /*38*/new TestDatum( "a b c ab ac bc abc", 239.42535182578533),
        /*39*/new TestDatum( "ab b", 161.374713902864),
        /*39*/new TestDatum( "ab b", 161.374713902864),
        /*40*/new TestDatum( "a ab b", 151.7818962901353),
        /*41*/new TestDatum( "bc a b ", 211.81155791985697),
        /*41*/new TestDatum( "bc a b ", 211.81155791985697),
        /*42*/new TestDatum( "a ab c", 217.76153949873623),
        /*42*/new TestDatum( "a ab c", 217.76153949873623),
        /*43*/new TestDatum( "a abc abcd", 378.35206382910627),
        /*43*/new TestDatum( "a abc abcd", 378.35206382910627),
        /*44*/new TestDatum( "abc b c ab ac bc", 341.95986349760267),
        /*44*/new TestDatum( "abc b c ab ac bc", 341.95986349760267),
        /*45*/new TestDatum( "a b c ab ac bc", 318.62457672968225),
        /*45*/new TestDatum( "a b c ab ac bc", 318.62457672968225),
        /*46*/new TestDatum( "a b c ab ac abc", 344.7353256172181),
        /*46*/new TestDatum( "a b c ab ac abc", 344.7353256172181),
        /*47*/new TestDatum( "a b ab ac bc abc", 341.95986357965387),
        /*47*/new TestDatum( "a b ab ac bc abc", 341.95986357965387),
        /*48*/new TestDatum( "a b ab c ac bc abc d ad bd abd cd acd bcd abcd", 480.2882471117551),
        /*49*/new TestDatum( "a b ab c ac bc abc cd acd bcd abcd cde acde bcde abcde", 501.609114069913),
        /*49*/new TestDatum( "a b ab c ac bc abc cd acd bcd abcd cde acde bcde abcde", 501.609114069913),
        /*50*/new TestDatum( "a b ab c ac bc abc d ad bd abd cd acd bcd abcd cde acde bcde abcde", 633.810500520683),
        /*50*/new TestDatum( "a b ab c ac bc abc d ad bd abd cd acd bcd abcd cde acde bcde abcde", 633.810500520683),
        /*51*/new TestDatum( "abcd abce", 508.7862565358031),
        /*51*/new TestDatum( "abcd abce", 508.7862565358031),
        /*52*/new TestDatum( "a ab c cd", 307.1661520191668),
        /*52*/new TestDatum( "a ab c cd", 307.1661520191668),
        /*53*/new TestDatum( "a c ab bc", 225.14440110878547),
        /*54*/new TestDatum( "a b ac bc bcd d", 458.5317063123452),
        /*54*/new TestDatum( "a b ac bc bcd d", 458.5317063123452),
        /*55*/new TestDatum( "abcd abce de", 996.2883327546962),
        /*55*/new TestDatum( "abcd abce de", 996.2883327546962),
        /*56*/new TestDatum( "a b ab c ac bc abc df adf bdf abdf cd acd bcd abcd cde acde bcde abcde", 841.2281428103137),
        /*56*/new TestDatum( "a b ab c ac bc abc df adf bdf abdf cd acd bcd abcd cde acde bcde abcde", 841.2281428103137),
        /*57*/new TestDatum( "abd abc dc", 610.4957135939354),
        /*57*/new TestDatum( "abd abc dc", 610.4957135939354),
        /*58*/new TestDatum( "a b ab c ac bc abc p q pq r pr qr pqr x bx px", 749.3046043410096),
        /*58*/new TestDatum( "a b ab c ac bc abc p q pq r pr qr pqr x bx px", 749.3046043410096),
        /*59*/new TestDatum( "a b ab c ac d ad e ae f af", 623.4193360283326),
        /*60*/new TestDatum( "a b c d cd ae be e ce de cde", 473.4313453028586),
        /*61*/new TestDatum( "a b c d cd ae be e ce de cde ef", 624.755861723394),
        /*62*/new TestDatum( "a b c ab ac bc abc ad", 353.72951221911853),
        /*62*/new TestDatum( "a b c ab ac bc abc ad", 353.72951221911853),
        /*63*/new TestDatum( "a b c ab ac bc abc abd", 340.82499327267107),
        /*63*/new TestDatum( "a b c ab ac bc abc abd", 340.82499327267107),
        /*64*/new TestDatum( "a b c ab ac bc abc abcd", 339.83033139030465),
        /*64*/new TestDatum( "a b c ab ac bc abc abcd", 339.83033139030465),
        /*65*/new TestDatum( "ad bd cd abd acd bcd abcd d", 389.29950581143595),
        /*65*/new TestDatum( "ad bd cd abd acd bcd abcd d", 389.29950581143595),
        /*66*/new TestDatum( "a b c ab ac bc abc ad bd cd", 579.5832494842473),
        /*66*/new TestDatum( "a b c ab ac bc abc ad bd cd", 579.5832494842473),
        /*67*/new TestDatum( "a b c ab ac bc abc abd bcd acd", 588.374389209913),
        /*67*/new TestDatum( "a b c ab ac bc abc abd bcd acd", 588.374389209913),
        /*68*/new TestDatum( "a b c ab ac bc abc ad d", 329.37716678546525),
        /*69*/new TestDatum( "a b c ab ac bc abc ad abd", 346.01510503713456),
        /*69*/new TestDatum( "a b c ab ac bc abc ad abd", 346.01510503713456),
        /*70*/new TestDatum( "a b c ab ac bc abc abd abcd", 340.75455505723016),
        /*70*/new TestDatum( "a b c ab ac bc abc abd abcd", 340.75455505723016),
        /*71*/new TestDatum( "a b c ab ac bc abc ad d be e cf f", 554.2867469715592),
        /*72*/new TestDatum( "a b c ab ac bc abc ad bd abd d", 355.41370661062354),
        /*73*/new TestDatum( "a b c ab ac bc abc acd bcd abcd cd", 365.61327622737673),
        /*74*/new TestDatum( "a ab b ac c ad d be e cf f dg g", 739.1650874845034),
        /*75*/new TestDatum( "a ab b ac c ad d be e cf f dg g eh h fi i gj j ak k kl l lm m", 2755.650253481586),
        /*76*/new TestDatum( "ab ac abc ad ae ade", 487.10424505218145),
        /*76*/new TestDatum( "ab ac abc ad ae ade", 487.10424505218145),
        /*77*/new TestDatum( "a b ab c ac abd ace", 469.1005971050783),
        /*77*/new TestDatum( "a b ab c ac abd ace", 469.1005971050783),
        /*78*/new TestDatum( "a b ab c ac d ad be ce de", 913.593066743922),
        /*78*/new TestDatum( "a b ab c ac d ad be ce de", 913.593066743922),
        /*79*/new TestDatum( "a b ab c ac d ad ae be ce de", 955.963468227657),
        /*79*/new TestDatum( "a b ab c ac d ad ae be ce de", 955.963468227657),
        /*80*/new TestDatum( "a b ab c ac abd ace acef acefg", 790.8953195743954),
        /*80*/new TestDatum( "a b ab c ac abd ace acef acefg", 790.8953195743954),
        /*81*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg", 2881.4574729004075),
        /*81*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg", 2881.4574729004075),
        /*82*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc", 3168.2249837673676),
        /*82*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc", 3168.2249837673676),
        /*83*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj", 3771.9321696318084),
        /*83*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj", 3771.9321696318084),
        /*84*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l", 4327.697005220982),
        /*84*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l", 4327.697005220982),
        /*85*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc", 4239.880685561009),
        /*85*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc", 4239.880685561009),
        /*86*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al", 5991.800650112288),
        /*86*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al", 5991.800650112288),
        /*87*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al m mn nc bc bco bo boj bp bop cq cqb rs ra s", 0.0),
        /*87*/new TestDatum( "qh h fh ih ik kh b ab ac de bd  abc bfg fc bj l lc al m mn nc bc bco bo boj bp bop cq cqb rs ra s", 0.0),
        /*88*/new TestDatum( ",", 0.0),
        /*89*/new TestDatum( ",.", 0.0),
        /*90*/new TestDatum( "a,", 80.35747263647977),
        /*91*/new TestDatum( "a,.", 80.35747263647977),
        /*92*/new TestDatum( "a,a", 80.38754263647976),
        /*93*/new TestDatum( "a,. a", 80.38754263647976),
        /*94*/new TestDatum( "a b ab,", 151.7818962901353),
        /*95*/new TestDatum( "a b ab, a", 151.8119662901353),
        /*96*/new TestDatum( "a b ab, b", 151.81227629013532),
        /*97*/new TestDatum( "a b ab, ab", 151.8432282901353),
        /*98*/new TestDatum( "a b ab, .", 151.7818962901353),
        /*99*/new TestDatum( "a b ab, . a", 151.8119662901353),
        /*100*/new TestDatum( "a b ab, . b", 151.81227629013532),
        /*101*/new TestDatum( "a b ab, . a b", 151.8423462901353),
        /*102*/new TestDatum( "a b ab, . a b ab", 151.9036782901353),
        /*103*/new TestDatum( "a ab c abc, ", 248.06646143598684),
        /*104*/new TestDatum( "a ab c abc,.", 248.06646143598684),
        /*105*/new TestDatum( "a ab c abc,a", 248.09653143598683),
        /*106*/new TestDatum( "a ab c abc,ab", 248.12779343598683),
        /*107*/new TestDatum( "a ab c abc,a ab", 248.15786343598683),
        /*108*/new TestDatum( "a b ab, ,a 'my_label", 310.9439177650555),
        /*109*/new TestDatum( "a b ab, ,b 'label2", 234.9945557984952),
        /*110*/new TestDatum( "a b ab, ,a 'sa, b 'sb", 425.9889815683995),
        /*111*/new TestDatum( "a b ab, ,., a", 509.99511776505557),
        /*112*/new TestDatum( "a b ab, ,ab", 272.96923678177535),
        /*113*/new TestDatum( "a b ab, ,a, ab", 463.9636625516796),
        /*114*/new TestDatum( "a b ab, ,b, ab", 372.8244281918072),
        /*115*/new TestDatum( "a b ab, ,a, ab, .", 692.2760158039885),
        /*116*/new TestDatum( "a b ab, ,a, ab, ., b", 883.5874992151189),
        /*117*/new TestDatum( "a b ab, ,a b", 743.9813858633836),
        /*118*/new TestDatum( "a b ab, ,a b, . ab", 1586.4416393021174),
        /*118*/new TestDatum( "a b ab, ,a b, . ab", 1586.4416393021174),
        /*119*/new TestDatum( "a b c ab ac bc abc ad, ,a b c abc ac", 0.0),
        /*119*/new TestDatum( "a b c ab ac bc abc ad, ,a b c abc ac", 0.0),
        /*120*/new TestDatum( "a b c ab ac bc abc ad, ,a b c abc", 0.0),
        /*120*/new TestDatum( "a b c ab ac bc abc ad, ,a b c abc", 0.0),
        /*121*/new TestDatum( "A B AB, ,A B, AB", 983.6086195810333),
        /*121*/new TestDatum( "A B AB, ,A B, AB", 983.6086195810333),
        /*122*/new TestDatum( "A B AB, ,A, AB", 463.9636578630396),
        /*123*/new TestDatum( "A B AB, ,B, AB", 372.8244235031673),
        /*124*/new TestDatum( "A B AB, ,A AB,B AB", 0.0),
        /*124*/new TestDatum( "A B AB, ,A AB,B AB", 0.0),
        /*125*/new TestDatum( "A B AB, ,A AB,B", 1086.8997518555555),
        /*126*/new TestDatum( "A B AB, ,B AB,A", 971.4567216663836),
        /*127*/new TestDatum( "A B C AB AC BC ABC, B,A AB ABC, B, B", 0.0),
        /*127*/new TestDatum( "A B C AB AC BC ABC, B,A AB ABC, B, B", 0.0),
        /*128*/new TestDatum( ",", 0.0),
        // Removed from test data as it's invalid microsyntax /*129*/new TestDatum( ",,", 0.0),
        /*130*/new TestDatum( "a,,a .", 845.5434726364797),
        /*131*/new TestDatum( "a,.,a .,.", 1055.5206726364797),
        /*132*/new TestDatum( "a,.,a .,.,.,.", 2033.7907206364794),
        /*133*/new TestDatum( "A B AB, ,A B AB .,B AB", 0.0),
        /*133*/new TestDatum( "A B AB, ,A B AB .,B AB", 0.0),

    };
}
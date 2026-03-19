package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 코디 도메인(TPO/스타일/의류속성) 전용 enum 모음.
 */

public final class CoordinationEnums {
    // 유틸성 클래스 인스턴스화를 방지한다.
    private CoordinationEnums() {
    }

    /*
     *Enum  목록 
     * 1. TpoType: 약속/출근/데이트 등 상황(TPO) 분류 코드
     * 2. GenderType: 성별 분류 코드
     * 3. StyleType: 코디 무드/스타일 분류 코드
     * 4. ColorType: 주요 색상 분류 코드
     * 5. PatternType: 패턴 분류 코드
     * 6. MaterialType: 의류 소재 분류 코드
     * 7. BrandType: 주요 브랜드 분류 코드 (각 카테고리별 유명 브랜드 매핑)
     * 8. FitType: 의류 핏 분류 코드
     * 9. ItemCategoryType: 실제 아이템 카테고리 분류 코드(검색/매핑용)
     * 10. CategoryType: 코디 슬롯 키 분류 코드(tops/bottom
     * outerwear/shoes/accessories)
     * 11. PriorityType: 슬롯별 필수도 분류 코드
     */

    // 약속/출근/캐주얼 등 상황(TPO) 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum TpoType implements CodedKeywordedEnum {
        DAILY("daily", "일상"),
        OFFICE("office", "출근"),
        DATE("date", "데이트"),
        WEDDING("wedding", "하객"),
        FORMAL("formal", "격식"),
        EXERCISE("exercise", "운동"),
        TRAVEL("travel", "여행"),
        CAMPING("camping", "캠핑");

        private final String code;
        private final String keyword;

        // @Getter가 getCode()와 getKeyword()를 자동으로 만듭니다.
        // @RequiredArgsConstructor가 (String code, String keyword) 생성자를 자동으로 만듭니다.

        @JsonCreator
        public static TpoType fromCode(String rawCode) {
            return EnumResolver.fromCode(TpoType.class, rawCode);
        }
    }

    // 성별 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum GenderType implements CodedKeywordedEnum {
        MALE("male", "남성"),
        FEMALE("female", "여성"),
        UNISEX("unisex", "유니섹스");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static GenderType fromCode(String rawCode) {
            return EnumResolver.fromCode(GenderType.class, rawCode);
        }
    }

    // 코디 무드/스타일 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum StyleType implements CodedKeywordedEnum {
        MINIMAL("minimal", "미니멀"),
        CASUAL("casual", "캐주얼"),
        STREET("street", "스트릿"),
        CLASSIC("classic", "클래식"),
        LOVELY("lovely", "러블리"),
        AMIKAJI("amikaji", "아메카지"),
        GORPCORE("gorpcore", "고프코어"),
        CHIC("chic", "시크");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static StyleType fromCode(String rawCode) {
            return EnumResolver.fromCode(StyleType.class, rawCode);
        }
    }

    // 주요 색상 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum ColorType implements CodedKeywordedEnum {
        BLACK("black", "블랙"),
        WHITE("white", "화이트"),
        GRAY("gray", "그레이"),
        NAVY("navy", "네이비"),
        BLUE("blue", "블루"),
        BEIGE("beige", "베이지"),
        BROWN("brown", "브라운"),
        KHAKI("khaki", "카키"),
        GREEN("green", "그린"),
        RED("red", "레드"),
        PINK("pink", "핑크"),
        PURPLE("purple", "퍼플"),
        YELLOW("yellow", "옐로우"),
        ORANGE("orange", "오렌지");

        private final String code;
        private final String keyword;

        // @Getter가 getCode()와 getKeyword()를 자동으로 생성합니다.
        // @RequiredArgsConstructor가 (String code, String keyword) 생성자를 자동으로 생성합니다.

        @JsonCreator
        public static ColorType fromCode(String rawCode) {
            return EnumResolver.fromCode(ColorType.class, rawCode);
        }
    }

    // 패턴 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum PatternType implements CodedKeywordedEnum {
        SOLID("solid", "무지"),
        STRIPE("stripe", "스트라이프"),
        CHECK("check", "체크"),
        PRINT("print", "프린팅"),
        GRAPHIC("graphic", "그래픽"),
        FLORAL("floral", "꽃무늬"),
        LOGO("logo", "로고"),
        CAMO("camo", "카모");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static PatternType fromCode(String rawCode) {
            return EnumResolver.fromCode(PatternType.class, rawCode);
        }
    }

    // 의류 소재 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum MaterialType implements CodedKeywordedEnum {
        COTTON("cotton", "면"),
        DENIM("denim", "데님"),
        WOOL("wool", "울"),
        CASHMERE("cashmere", "캐시미어"),
        LEATHER("leather", "가죽"),
        SUEDE("suede", "스웨이드"),
        LINEN("linen", "린넨"),
        NYLON("nylon", "나일론"),
        FLEECE("fleece", "기모"), // 네이버 쇼핑 겨울 필수 키워드
        CORDUROY("corduroy", "코듀로이"),
        POLYESTER("polyester", "폴리"),
        SILK("silk", "실크");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static MaterialType fromCode(String rawCode) {
            return EnumResolver.fromCode(MaterialType.class, rawCode);
        }
    }

    // 주요 브랜드 분류 코드 (각 카테고리별 유명 브랜드 매핑)
    @Getter
    @RequiredArgsConstructor
    public enum BrandType implements CodedKeywordedEnum {
        // SPA (가성비/기본템)
        MUSINSA_STANDARD("mustand", "무신사스탠다드"),
        SPAO("spao", "스파오"),
        EIGHT_SECONDS("8seconds", "에잇세컨즈"),
        GIORDANO("giordano", "지오다노"),
        UNIQLO("uniqlo", "유니클로"),
        ZARA("zara", "자라"),
        H_AND_M("hm", "H&M"),

        // CASUAL / DOMESTIC (트렌디 도메스틱)
        THISISNEVERTHAT("thisisneverthat", "디스이즈네버댓"),
        COVERNAT("covernat", "커버낫"),
        LMC("lmc", "LMC"),
        VIVASTUDIO("vivastudio", "비바스튜디오"),
        ANDERSON_BELL("anderssonbell", "앤더슨벨"),
        INSILENCE("insilence", "인사일런스"),
        COOR("coor", "쿠어"),

        // CONTEMPORARY (미니멀/고급)
        COS("cos", "COS"),
        MAISON_KITSUNE("kitsune", "메종키츠네"),
        AMI("ami", "아미"),
        APC("apc", "A.P.C."),
        THEORY("theory", "띠어리"),
        SYSTEM_HOMME("systemhomme", "시스템옴므"),

        // SPORTS / OUTDOOR (기능성/고프코어)
        NIKE("nike", "나이키"),
        ADIDAS("adidas", "아디다스"),
        NEW_BALANCE("newbalance", "뉴발란스"),
        NORTH_FACE("northface", "노스페이스"),
        ARCTERYX("arcteryx", "아크테릭스"),
        PATAGONIA("patagonia", "파타고니아"),
        I_AM_SHOP("iamshop", "아이앱스튜디오");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static BrandType fromCode(String rawCode) {
            return EnumResolver.fromCode(BrandType.class, rawCode);
        }
    }

    // 의류 핏 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum FitType implements CodedKeywordedEnum { // 자식만 구현하면 됨
        SLIM("slim", "슬림핏"),
        STANDARD("standard", "스탠다드핏"),
        SEMI_OVER("semi_over", "세미오버핏"),
        OVERSIZED("oversized", "오버핏"),
        WIDE("wide", "와이드핏"),
        BALLOON("balloon", "벌룬핏"),
        TAPERED("tapered", "테이퍼드핏"),
        CROP("crop", "크롭");

        private final String code;
        private final String keyword;

        // Lombok @Getter가 getCode()와 getKeyword()를 만들어서
        // 인터페이스의 추상 메서드들을 자동으로 충족시킵니다.

        @JsonCreator
        public static FitType fromCode(String rawCode) {
            return EnumResolver.fromCode(FitType.class, rawCode);
        }
    }

    // 실제 아이템 카테고리 분류 코드(검색/매핑용)
    @Getter
    @RequiredArgsConstructor
    public enum ItemCategoryType implements CodedKeywordedEnum {
        // 상의류
        T_SHIRT("t_shirt", "티셔츠"),
        SWEATSHIRT("sweatshirt", "맨투맨"),
        HOODIE("hoodie", "후드티"),
        KNIT("knit", "니트"),
        SHIRT("shirt", "셔츠"),
        BLOUSE("blouse", "블라우스"),
        ONE_PIECE("one_piece", "원피스"),

        // 하의류
        SLACKS("slacks", "슬랙스"),
        JEANS("jeans", "청바지"),
        CHINOS("chinos", "치노팬츠"),
        JOGGERS("joggers", "조거팬츠"),
        SHORTS("shorts", "반바지"),
        SKIRT("skirt", "스커트"),
        MINI_SKIRT("mini_skirt", "미니스커트"),
        LONG_SKIRT("long_skirt", "롱스커트"),
        LEGGINGS("leggings", "레깅스"),

        // 아우터
        COAT("coat", "코트"),
        JACKET("jacket", "자켓"),
        PADDING("padding", "패딩"),
        CARDIGAN("cardigan", "가디건"),
        WIND_BREAKER("windbreaker", "바람막이"),

        // 신발
        SNEAKERS("sneakers", "스니커즈"),
        RUNNING_SHOES("running_shoes", "런닝화"),
        LOAFERS("loafers", "로퍼"),
        DERBY_SHOES("derby", "더비슈즈"),
        FLAT_SHOES("flat_shoes", "플랫슈즈"),
        BOOTS("boots", "부츠"),
        SANDALS("sandals", "샌들"),
        RAIN_BOOTS("rain_boots", "레인부츠"),
        SLIPPER("slipper", "슬리퍼"),

        // Headwear
        BALL_CAP("ball_cap", "볼캡"),
        BUCKET_HAT("bucket_hat", "버킷햇"),
        BEANIE("beanie", "비니"),
        CAMP_CAP("camp_cap", "캠프캡"),
        BERET("beret", "베레모"),

        // 액세서리
        BACKPACK("backpack", "백팩"),
        TOTE_BAG("tote_bag", "토트백"),
        CROSS_BAG("cross_bag", "크로스백"),
        MESSENGER_BAG("messenger", "메신저백"),
        SHOULDER_BAG("shoulder", "숄더백"),
        HOBO_BAG("hobo_bag", "호보백"),
        MINI_BAG("mini_bag", "미니백"),
        ECO_BAG("eco_bag", "에코백"),
        UMBRELLA("umbrella", "우산"),
        GLASSES("glasses", "안경"),
        MUFFLER("muffler", "목도리"),
        GLOVES("gloves", "장갑");

        private final String code;
        private final String keyword; // QueryExtractor가 바로 가져다 쓸 한글

        @JsonCreator
        public static ItemCategoryType fromCode(String rawCode) {
            return EnumResolver.fromCode(ItemCategoryType.class, rawCode);
        }
    }

    // 코디 슬롯 키 분류 코드(tops/bottoms/outerwear/shoes/accessories)
    @Getter
    @RequiredArgsConstructor
    public enum CategoryType implements CodedKeywordedEnum {
        HEADWEAR("headwear", "모자"),
        TOPS("tops", "상의"),
        BOTTOMS("bottoms", "하의"),
        OUTERWEAR("outerwear", "아우터"),
        SHOES("shoes", "신발"),
        ACCESSORIES("accessories", "액세서리");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static CategoryType fromCode(String rawCode) {
            return EnumResolver.fromCode(CategoryType.class, rawCode);
        }
    }

    // 슬롯별 필수도 분류 코드
    @Getter
    @RequiredArgsConstructor
    public enum PriorityType implements CodedKeywordedEnum {
        ESSENTIAL("essential", "필수"),
        OPTIONAL("optional", "선택");

        private final String code;
        private final String keyword;

        @JsonCreator
        public static PriorityType fromCode(String rawCode) {
            return EnumResolver.fromCode(PriorityType.class, rawCode);
        }
    }
}

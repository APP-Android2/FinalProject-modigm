package kr.co.lion.modigm.util

enum class Skill(val num: Int, val displayName: String, val category: Category?) {
    // 프로그래밍 언어
    PYTHON(1, "Python", Category.PROGRAMMING),
    JAVA(2, "Java", Category.PROGRAMMING),
    CSHARP(3, "C#", Category.PROGRAMMING),
    JAVASCRIPT_PROGRAMMING(4, "JavaScript", Category.PROGRAMMING),
    RUBY(5, "Ruby", Category.PROGRAMMING),
    GO(6, "Go", Category.PROGRAMMING),
    SWIFT(7, "Swift", Category.PROGRAMMING),
    KOTLIN(8, "Kotlin", Category.PROGRAMMING),
    R(9, "R", Category.PROGRAMMING),
    CPP(10, "C++", Category.PROGRAMMING),
    PHP(11, "PHP", Category.PROGRAMMING),
    RUST(12, "Rust", Category.PROGRAMMING),
    TYPESCRIPT(13, "TypeScript", Category.PROGRAMMING),
    OTHER_PROGRAMMING(63, "기타", Category.PROGRAMMING),

    // 프론트 엔드
    HTML(14, "HTML", Category.FRONT_END),
    CSS(15, "CSS", Category.FRONT_END),
    JAVASCRIPT_FRONT(16, "JavaScript", Category.FRONT_END),
    REACT(17, "React", Category.FRONT_END),
    ANGULAR(18, "Angular", Category.FRONT_END),
    VUEJS(19, "Vue.js", Category.FRONT_END),
    NPM(20, "NPM", Category.FRONT_END),
    WEBPACK(21, "Webpack", Category.FRONT_END),
    BABEL(22, "Babel", Category.FRONT_END),
    OTHER_FRONT_END(64, "기타", Category.FRONT_END),

    // 백엔드
    JAVA_BACKEND(23, "Java", Category.BACK_END),
    PYTHON_BACKEND(24, "Python", Category.BACK_END),
    RUBY_BACKEND(25, "Ruby", Category.BACK_END),
    NODEJS(26, "Node.js", Category.BACK_END),
    PHP_BACKEND(27, "PHP", Category.BACK_END),
    CSHARP_BACKEND(28, "C#", Category.BACK_END),
    EXPRESS(29, "Express", Category.BACK_END),
    DJANGO(30, "Django", Category.BACK_END),
    FLASK(31, "Flask", Category.BACK_END),
    SPRING(32, "Spring", Category.BACK_END),
    DOTNET(33, ".NET", Category.BACK_END),
    MYSQL(34, "MySQL", Category.BACK_END),
    POSTGRESQL(35, "PostgreSQL", Category.BACK_END),
    MONGODB(36, "MongoDB", Category.BACK_END),
    REDIS(37, "Redis", Category.BACK_END),
    OTHER_BACKEND(65, "기타", Category.BACK_END),

    // 모바일 개발
    SWIFT_IOS(38, "Swift(iOS)", Category.MOBILE),
    KOTLIN_ANDROID(39, "Kotlin(Android)", Category.MOBILE),
    REACT_NATIVE(40, "React Native", Category.MOBILE),
    FLUTTER(41, "Flutter", Category.MOBILE),
    XAMARIN(42, "Xamarin", Category.MOBILE),
    OTHER_MOBILE(66, "기타", Category.MOBILE),

    // 데이터 사이언스
    PANDAS(43, "Pandas", Category.DATA_SCIENCE),
    NUMPY(44, "NumPy", Category.DATA_SCIENCE),
    SCIPY(45, "SciPy", Category.DATA_SCIENCE),
    SCIKIT_LEARN(46, "scikit-learn", Category.DATA_SCIENCE),
    TENSORFLOW(47, "TensorFlow", Category.DATA_SCIENCE),
    PYTORCH(48, "PyTorch", Category.DATA_SCIENCE),
    JUPYTER(49, "Jupyter Notebook", Category.DATA_SCIENCE),
    ANACONDA(50, "Anaconda", Category.DATA_SCIENCE),
    OTHER_DATA_SCIENCE(67, "기타", Category.DATA_SCIENCE),

    // 데브옵스 및 시스템 관리
    JENKINS(51, "Jenkins", Category.DEVOPS),
    ANSIBLE(52, "Ansible", Category.DEVOPS),
    TERRAFORM(53, "Terraform", Category.DEVOPS),
    DOCKER(54, "Docker", Category.DEVOPS),
    KUBERNETES(55, "Kubernetes", Category.DEVOPS),
    PROMETHEUS(56, "Prometheus", Category.DEVOPS),
    GRAFANA(57, "Grafana", Category.DEVOPS),
    ELK_STACK(58, "ELK Stack", Category.DEVOPS),
    OTHER_DEVOPS(68, "기타", Category.DEVOPS),

    // 클라우드 및 인프라
    AWS(59, "AWS", Category.CLOUD),
    GOOGLE_CLOUD(60, "Google Cloud", Category.CLOUD),
    AZURE(61, "Azure", Category.CLOUD),
    AWS_LAMBDA(62, "AWS Lambda", Category.CLOUD),
    AZURE_FUNCTIONS(63, "Azure Functions", Category.CLOUD),
    CLOUDFORMATION(64, "CloudFormation", Category.CLOUD),
    OTHER_CLOUD(69, "기타", Category.CLOUD),

    // 게임 개발
    UNITY(70, "Unity", Category.GAME_DEVELOPMENT),
    UNREAL_ENGINE(71, "Unreal Engine", Category.GAME_DEVELOPMENT),
    BLENDER(72, "Blender", Category.GAME_DEVELOPMENT),
    MAYA(73, "Maya", Category.GAME_DEVELOPMENT),
    OTHER_GAME_DEV(74, "기타", Category.GAME_DEVELOPMENT),

    // 보안
    WIRESHARK(75, "Wireshark", Category.SECURITY),
    METASPLOIT(76, "Metasploit", Category.SECURITY),
    AUTHENTICATION(77, "인증", Category.SECURITY),
    NETWORK_SECURITY(78, "네트워크 보안", Category.SECURITY),
    KALI_LINUX(79, "Kali Linux", Category.SECURITY),
    OWASP(80, "OWASP", Category.SECURITY),
    OTHER_SECURITY(81, "기타", Category.SECURITY),

    // 인공지능
    AI_PYTHON(82, "Python", Category.AI),
    AI_R(83, "R", Category.AI),
    MACHINE_LEARNING(84, "머신러닝", Category.AI),
    COMPUTER_VISION(85, "컴퓨터 비전", Category.AI),
    NLP(86, "자연어 처리", Category.AI),
    OTHER_AI(87, "기타", Category.AI),

    // UI/UX 디자인
    SKETCH(88, "Sketch", Category.UI_UX),
    ADOBE_XD(89, "Adobe XD", Category.UI_UX),
    FIGMA(90, "Figma", Category.UI_UX),
    USER_CENTERED_DESIGN(91, "사용자 중심 디자인", Category.UI_UX),
    INTERACTION_DESIGN(92, "인터랙션 디자인", Category.UI_UX),
    OTHER_UI_UX(93, "기타", Category.UI_UX),

    // 빅데이터
    HADOOP(94, "Hadoop", Category.BIG_DATA),
    SPARK(95, "Spark", Category.BIG_DATA),
    HDFS(96, "HDFS", Category.BIG_DATA),
    CASSANDRA(97, "Cassandra", Category.BIG_DATA),
    APACHE_KAFKA(98, "Apache Kafka", Category.BIG_DATA),
    APACHE_FLINK(99, "Apache Flink", Category.BIG_DATA),
    OTHER_BIG_DATA(100, "기타", Category.BIG_DATA),

    // Special categories
    TOTAL(101, "전체", null),
    OTHER(102, "기타", null);

    enum class Category {
        PROGRAMMING,
        FRONT_END,
        BACK_END,
        MOBILE,
        DATA_SCIENCE,
        DEVOPS,
        CLOUD,
        GAME_DEVELOPMENT,
        SECURITY,
        AI,
        UI_UX,
        BIG_DATA,
        OTHER
    }

    companion object {
        fun fromNum(num: Int): Skill {
            return values().find { it.num == num } ?: OTHER
        }
    }
}

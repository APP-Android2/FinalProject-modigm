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

    // 프론트 엔드
    HTML(15, "HTML", Category.FRONT_END),
    CSS(16, "CSS", Category.FRONT_END),
    JAVASCRIPT_FRONT(17, "JavaScript", Category.FRONT_END),
    REACT(18, "React", Category.FRONT_END),
    ANGULAR(19, "Angular", Category.FRONT_END),
    VUEJS(20, "Vue.js", Category.FRONT_END),
    NPM(21, "NPM", Category.FRONT_END),
    WEBPACK(22, "Webpack", Category.FRONT_END),
    BABEL(23, "Babel", Category.FRONT_END),

    // 백엔드
    JAVA_BACKEND(25, "Java", Category.BACK_END),
    PYTHON_BACKEND(26, "Python", Category.BACK_END),
    RUBY_BACKEND(27, "Ruby", Category.BACK_END),
    NODEJS(28, "Node.js", Category.BACK_END),
    PHP_BACKEND(29, "PHP", Category.BACK_END),
    CSHARP_BACKEND(30, "C#", Category.BACK_END),
    EXPRESS(31, "Express", Category.BACK_END),
    DJANGO(32, "Django", Category.BACK_END),
    FLASK(33, "Flask", Category.BACK_END),
    SPRING(34, "Spring", Category.BACK_END),
    DOTNET(35, ".NET", Category.BACK_END),
    MYSQL(36, "MySQL", Category.BACK_END),
    POSTGRESQL(37, "PostgreSQL", Category.BACK_END),
    MONGODB(38, "MongoDB", Category.BACK_END),
    REDIS(39, "Redis", Category.BACK_END),

    // 모바일 개발
    SWIFT_IOS(41, "Swift(iOS)", Category.MOBILE),
    KOTLIN_ANDROID(42, "Kotlin(Android)", Category.MOBILE),
    REACT_NATIVE(43, "ReactNative", Category.MOBILE),
    FLUTTER(44, "Flutter", Category.MOBILE),
    XAMARIN(45, "Xamarin", Category.MOBILE),

    // 데이터 사이언스
    PANDAS(47, "Pandas", Category.DATA_SCIENCE),
    NUMPY(48, "NumPy", Category.DATA_SCIENCE),
    SCIPY(49, "SciPy", Category.DATA_SCIENCE),
    SCIKIT_LEARN(50, "scikit-learn", Category.DATA_SCIENCE),
    TENSORFLOW(51, "TensorFlow", Category.DATA_SCIENCE),
    PYTORCH(52, "PyTorch", Category.DATA_SCIENCE),
    JUPYTER(53, "JupyterNotebook", Category.DATA_SCIENCE),
    ANACONDA(54, "Anaconda", Category.DATA_SCIENCE),

    // 데브옵스 및 시스템 관리
    JENKINS(56, "Jenkins", Category.DEVOPS),
    ANSIBLE(57, "Ansible", Category.DEVOPS),
    TERRAFORM(58, "Terraform", Category.DEVOPS),
    DOCKER(59, "Docker", Category.DEVOPS),
    KUBERNETES(60, "Kubernetes", Category.DEVOPS),
    PROMETHEUS(61, "Prometheus", Category.DEVOPS),
    GRAFANA(62, "Grafana", Category.DEVOPS),
    ELK_STACK(63, "ELKStack", Category.DEVOPS),

    // 클라우드 및 인프라
    AWS(65, "AWS", Category.CLOUD),
    GOOGLE_CLOUD(66, "GoogleCloud", Category.CLOUD),
    AZURE(67, "Azure", Category.CLOUD),
    AWS_LAMBDA(68, "AWSLambda", Category.CLOUD),
    AZURE_FUNCTIONS(69, "AzureFunctions", Category.CLOUD),
    CLOUDFORMATION(70, "CloudFormation", Category.CLOUD),

    // 게임 개발
    UNITY(72, "Unity", Category.GAME_DEVELOPMENT),
    UNREAL_ENGINE(73, "UnrealEngine", Category.GAME_DEVELOPMENT),
    BLENDER(74, "Blender", Category.GAME_DEVELOPMENT),
    MAYA(75, "Maya", Category.GAME_DEVELOPMENT),

    // 보안
    WIRESHARK(77, "Wireshark", Category.SECURITY),
    METASPLOIT(78, "Metasploit", Category.SECURITY),
    AUTHENTICATION(79, "인증", Category.SECURITY),
    NETWORK_SECURITY(80, "네트워크 보안", Category.SECURITY),
    KALI_LINUX(81, "KaliLinux", Category.SECURITY),
    OWASP(82, "OWASP", Category.SECURITY),

    // 인공지능
    AI_PYTHON(84, "Python", Category.AI),
    AI_R(85, "R", Category.AI),
    MACHINE_LEARNING(86, "머신러닝", Category.AI),
    COMPUTER_VISION(87, "컴퓨터 비전", Category.AI),
    NLP(88, "자연어 처리", Category.AI),

    // UI/UX 디자인
    SKETCH(90, "Sketch", Category.UI_UX),
    ADOBE_XD(91, "AdobeXD", Category.UI_UX),
    FIGMA(92, "Figma", Category.UI_UX),
    USER_CENTERED_DESIGN(93, "사용자 중심 디자인", Category.UI_UX),
    INTERACTION_DESIGN(94, "인터랙션 디자인", Category.UI_UX),

    // 빅데이터
    HADOOP(96, "Hadoop", Category.BIG_DATA),
    SPARK(97, "Spark", Category.BIG_DATA),
    HDFS(98, "HDFS", Category.BIG_DATA),
    CASSANDRA(99, "Cassandra", Category.BIG_DATA),
    APACHE_KAFKA(100, "ApacheKafka", Category.BIG_DATA),
    APACHE_FLINK(101, "ApacheFlink", Category.BIG_DATA),

    // Special categories
    TOTAL(103, "전체", null),
    OTHER(104, "기타", null);

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

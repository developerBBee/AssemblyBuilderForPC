# Maven / Kotlin セットアップ

Spring Boot + Kotlin を Maven で使うために必要な最小構成を示す。

## 追加する依存関係

`<dependencies>` 内に追加:

```xml
<!-- Kotlin stdlib (JDK 8+ API のラッパーを含む) -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>

<!-- Jackson の Kotlin サポート（JSON シリアライズ）-->
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
</dependency>

<!-- Kotlin リフレクション（Spring Bean 生成に必要）-->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
</dependency>
```

> バージョンは `spring-boot-starter-parent` が管理するため `<version>` 不要。

## ビルドプラグイン

`<build><plugins>` 内で `spring-boot-maven-plugin` と **同じ階層** に追加する。
Kotlin コンパイラは Java コンパイラより **先に** 動く必要があるため、
`kotlin-maven-plugin` を `maven-compiler-plugin` より前に書く。

```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <configuration>
        <args>
            <!-- JSR-305（@Nullable / @NonNull など）の nullability 解釈を strict にする -->
            <arg>-Xjsr305=strict</arg>
        </args>
        <compilerPlugins>
            <!-- Spring コンポーネントに open を自動付与（proxy 生成対応） -->
            <plugin>spring</plugin>
            <!-- JPA エンティティ用（JPAを使う場合のみ） -->
            <!-- <plugin>jpa</plugin> -->
        </compilerPlugins>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>compile</id>
            <goals><goal>compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/main/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
        <execution>
            <id>test-compile</id>
            <goals><goal>test-compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/test/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <executions>
        <!-- Java のデフォルト compile フェーズを無効化し、Kotlin の後に動かす -->
        <execution>
            <id>default-compile</id>
            <phase>none</phase>
        </execution>
        <execution>
            <id>default-testCompile</id>
            <phase>none</phase>
        </execution>
        <execution>
            <id>java-compile</id>
            <phase>compile</phase>
            <goals><goal>compile</goal></goals>
        </execution>
        <execution>
            <id>java-test-compile</id>
            <phase>test-compile</phase>
            <goals><goal>testCompile</goal></goals>
        </execution>
    </executions>
</plugin>
```

## 注意点

- **混在ビルド（Java + Kotlin）**: 上記スニペットでは Kotlin ファイルも `src/main/java/` 配下に置く前提で `sourceDir` を設定している。`src/main/kotlin/` を別途設けたい場合は `<sourceDir>` にそのパスも追加すること。完全移行後は Java の `sourceDir` エントリを削除してよい。
- **`-Xjsr305=strict`**: `@NonNull`/`@Nullable` アノテーションを Kotlin の
  non-null/nullable 型として厳格に扱う。Spring のアノテーションが正しく解釈される。
- **`spring` allopen プラグイン**: `@Component`, `@Service`, `@Controller` 等に
  自動で `open` を付与し、Spring の CGLIB プロキシが正常動作するようにする。

# Java → Kotlin 変換パターン集

## 1. 定数クラス → `object`

```java
// Java
public class ApiEndPoint {
    public static final String GET_DEVICE = "/devicelist";
    public static final String GET_UPDATE  = "/update";
}
```

```kotlin
// Kotlin
object ApiEndPoint {
    const val GET_DEVICE = "/devicelist"
    const val GET_UPDATE  = "/update"
}
```

- `public class` で全フィールドが `static final` → `object` に変換
- `static final` の String/数値リテラル → `const val`
- 他の型（リスト等）は `val` のみ（`const` 不可）

---

## 2. Java record → Kotlin data class

```java
// Java (シンプル)
public record UserAssem(String id, String deviceid, String device,
                        String guestid, LocalDateTime createddate,
                        LocalDateTime lastupdate) {}
```

```kotlin
// Kotlin
data class UserAssem(
    val id: String?,
    val deviceid: String?,
    val device: String?,
    val guestid: String?,
    val createddate: LocalDateTime?,
    val lastupdate: LocalDateTime?
)
```

### nullability の判断
- `@NonNull` アノテーション付き → `String`（non-null）
- アノテーションなし、または DB から取得する値 → `String?`（nullable）
- プリミティブ型（`int`, `boolean`等）→ Kotlin では `Int`, `Boolean`（non-null）
- ラッパー型（`Integer`, `Boolean`）→ `Int?`, `Boolean?`

### record のコンパニオンメソッド

```java
// Java
public record DeviceInfo(...) {
    private static final String DEFAULT = "20000101";

    @NonNull
    public static DeviceInfo from(@NonNull Map<String, Object> result) { ... }

    private static Integer toInteger(Object value) { ... }
}
```

```kotlin
// Kotlin
data class DeviceInfo(...) {
    companion object {
        private const val DEFAULT = "20000101"

        fun from(result: Map<String, Any>): DeviceInfo { ... }

        private fun toInteger(value: Any?): Int? { ... }
    }
}
```

- `static` フィールド・メソッド → `companion object` へ移動
- `static final` 定数 → `companion object` 内の `const val`
- `@NonNull` → non-null 型（`?` なし）
- `@NonNull` パラメータ → 呼び出し側はそのまま non-null で渡す

---

## 3. インターフェース → そのまま `interface`

```java
// Java
public interface GeminiService {
    ReviewResponse getReview(String prompt);
}
```

```kotlin
// Kotlin
interface GeminiService {
    fun getReview(prompt: String): ReviewResponse
}
```

- `interface` キーワードはそのまま
- メソッドシグネチャ → `fun` キーワードを先頭に追加
- 戻り値型を末尾に移動（`: ReturnType`）

---

## 4. ユーティリティクラス → トップレベル関数

```java
// Java
public class StringEncoder {
    public static String sjisToUtf8(String value) throws UnsupportedEncodingException { ... }
    private static String convert(String value, String src, String dest) throws UnsupportedEncodingException { ... }
}
```

```kotlin
// Kotlin
// パッケージ宣言の直下、クラス外に記述
fun sjisToUtf8(value: String): String { ... }

private fun convert(value: String, src: String, dest: String): String { ... }
```

- `public static` メソッドのみのクラス → クラス不要、トップレベル関数に
- `private static` → ファイルスコープの `private fun`
- `throws XxxException` → **削除**（Kotlin はチェック例外を持たない）
  - 実装内で例外が発生しうる場合はそのままスロー、あるいは `runCatching{}` でラップ
  - Java 呼び出し側から呼ばれる場合は `@Throws(XxxException::class)` を付与

---

## 5. Spring Boot クラスのパターン

### `@SpringBootApplication` メインクラス

```java
// Java
@SpringBootApplication
public class PcassemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PcassemApplication.class, args);
    }
}
```

```kotlin
// Kotlin
@SpringBootApplication
class PcassemApplication

fun main(args: Array<String>) {
    runApplication<PcassemApplication>(*args)
}
```

### Controller / Service / Component

```java
// Java
@RestController
@RequestMapping("/api")
public class ApiResponseController {
    private final DeviceInfoDao dao;

    public ApiResponseController(DeviceInfoDao dao) {
        this.dao = dao;
    }
}
```

```kotlin
// Kotlin（コンストラクタインジェクションが簡潔になる）
@RestController
@RequestMapping("/api")
class ApiResponseController(private val dao: DeviceInfoDao)
```

- コンストラクタインジェクション → プライマリコンストラクタの `val` パラメータに集約
- `@Autowired` はコンストラクタが1つなら不要
- Spring の CGLIB プロキシのために `open` が必要 → `kotlin-allopen` プラグイン（`spring` preset）が自動付与

---

## 6. よくある Kotlin イディオム

### `null` チェック

```java
// Java
if (value == null) return null;
return value.toString();
```

```kotlin
// Kotlin
return value?.toString()
```

### エルビス演算子（デフォルト値）

```java
// Java
return integer != null ? integer : defaultValue;
```

```kotlin
// Kotlin
return integer ?: defaultValue
```

### スマートキャスト（instanceof → is）

```java
// Java
if (value instanceof Integer integer) {
    return integer;
}
if (value instanceof Number number) {
    return number.intValue();
}
```

```kotlin
// Kotlin
return when (value) {
    is Int    -> value
    is Number -> value.toInt()
    else      -> value.toString().toInt()
}
```

### `when` 式（switch の置き換え）

```java
// Java
switch (type) {
    case "A": return 1;
    case "B": return 2;
    default:  return 0;
}
```

```kotlin
// Kotlin
return when (type) {
    "A"  -> 1
    "B"  -> 2
    else -> 0
}
```

### 文字列テンプレート

```java
// Java
throw new UnsupportedEncodingException("src=" + src + ",dest=" + dest);
```

```kotlin
// Kotlin
throw UnsupportedEncodingException("src=$src,dest=$dest")
```

### HashMap の初期化

```java
// Java
Map<String, String> map = new HashMap<>();
map.put("key", "value");
```

```kotlin
// Kotlin
val map = mutableMapOf("key" to "value")
// または
val map = hashMapOf("key" to "value")
```

---

## 7. アノテーション互換性

| Java                     | Kotlin                         | 備考 |
|--------------------------|--------------------------------|------|
| `@NonNull`               | Non-null 型（`String`）         | 型で表現するためアノテーション不要 |
| `@Nullable`              | Nullable 型（`String?`）        | 同上 |
| `@Component` 等          | そのまま使用可                  | allopen プラグインが `open` を付与 |
| `@JvmRecord`（Java 16+）  | 不要（Kotlin の `data class` を使う） | |
| `@JvmStatic`             | `companion object` 内のメソッドに付与 | Java から静的呼び出しされる場合のみ必要 |
| `@JvmField`              | `companion object` のフィールドに付与 | Java から `Foo.FIELD` でアクセスされる場合のみ必要 |
| `@Throws(IOException::class)` | Java から呼ばれる場合に付与 | Kotlin→Kotlin のみなら不要 |

---

## 8. ファイル配置

変換後のファイルは元の Java ファイルと **同じディレクトリ** に置く。

```
src/main/java/jp/developer/bbee/pcassem/
  ApiEndPoint.java      ← 削除
  ApiEndPoint.kt        ← 新規（同パッケージ）
```

パッケージ宣言はそのまま維持:
```kotlin
package jp.developer.bbee.pcassem.constants
```

Kotlin ファイルも `src/main/java/` 以下に置いてよい（Maven の `sourceDir` 設定による）。
または `src/main/kotlin/` を作って移すことも可能だが、混在プロジェクトでは `java/` のままが無難。

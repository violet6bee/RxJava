# Отчет по реализации упрощенной версии RxJava

## 1. Архитектура системы

### 1.1 Основные компоненты

**Реализованная реактивная библиотека состоит из следующих ключевых компонентов:**

- **Observable** - представляет поток данных, который может обрабатывать элементы (через `onNext`), ошибки (через `onError`) и сигнал о завершении (через `onComplete`).
- **Observer** - интерфейс для получения уведомлений от Observable:
  ```java
  public interface Observer<T> {
      void onSubscribe(Disposable d);
      void onNext(T item);
      void onError(Throwable e);
      void onComplete();
  }
  ```
- **Disposable** - механизм для управления подпиской и ресурсами:
  ```java
  public interface Disposable {
      void dispose();
      boolean isDisposed();
  }
  ```

### 1.2 Принцип работы

Библиотека реализует паттерн "Наблюдатель" с добавлением реактивных возможностей:
1. Создание Observable с помощью фабричных методов (`create`, `just`)
2. Применение операторов (`map`, `filter`, `flatMap`)
3. Подписка через метод `subscribe`
4. Управление потоками выполнения через `subscribeOn` и `observeOn`

## 2. Schedulers: принципы работы и применение

### 2.1 Реализованные Schedulers

```java
public class Schedulers {
    public static Scheduler io() {
        return new IoScheduler(); // Для IO-операций
    }
    
    public static Scheduler computation() {
        return new ComputationScheduler(); // Для вычислений
    }
}
```

### 2.2 Сравнение Schedulers

| Scheduler        | Пул потоков             | Использование                     |
|------------------|-------------------------|-----------------------------------|
| IO              | CachedThreadPool       | Сетевые запросы, работа с файлами|
| Computation     | FixedThreadPool (по ядрам CPU) | Вычислительные задачи       |

### 2.3 Пример использования

```java
Observable.just("data")
    .subscribeOn(Schedulers.io()) // Подписка в IO-потоке
    .map(data -> process(data))   // Тяжелые вычисления
    .observeOn(Schedulers.computation()) // Обработка в вычислительном потоке
    .subscribe(result -> updateUI(result));
```

## 3. Тестирование системы

### 3.1 Стратегия тестирования

1. **Модульные тесты** - проверка отдельных операторов
2. **Интеграционные тесты** - проверка комбинаций операторов
3. **Многопоточные тесты** - проверка работы Schedulers

### 3.2 Основные тестовые сценарии

```java
// Тест базовой функциональности
@Test
public void testMapOperator() {
    Observable.just(1, 2, 3)
        .map(x -> x * 2)
        .test()
        .assertValues(2, 4, 6);
}

// Тест обработки ошибок
@Test
public void testErrorHandling() {
    Observable.just(1, 0, 2)
        .map(x -> 10 / x)
        .test()
        .assertError(ArithmeticException.class);
}

// Тест многопоточности
@Test
public void testThreading() {
    Observable.just(1)
        .subscribeOn(Schedulers.io())
        .test()
        .awaitDone(1, TimeUnit.SECONDS)
        .assertComplete();
}
```

## 4. Примеры использования библиотеки

### 4.1 Простая цепочка операций

```java
Observable.just("apple", "banana", "cherry")
    .filter(fruit -> fruit.length() > 5)
    .map(String::toUpperCase)
    .subscribe(
        System.out::println,
        Throwable::printStackTrace,
        () -> System.out.println("Completed!")
    );
```

### 4.2 Комбинирование операторов

```java
Observable.range(1, 10)
    .flatMap(x -> Observable.just(x, x * 10))
    .take(5)
    .subscribe(System.out::println);
```

### 4.3 Асинхронная обработка

```java
Observable.fromCallable(() -> fetchDataFromNetwork())
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.computation())
    .subscribe(
        data -> processData(data),
        error -> showError(error)
    );
```

## Заключение

Реализованная библиотека предоставляет:
1. Основные операторы реактивного программирования
2. Гибкое управление потоками выполнения
3. Корректную обработку ошибок
4. Возможности композиции операторов


Пример использования в реальном приложении:
```java
// Загрузка и обработка данных
apiService.getUsers()
    .subscribeOn(Schedulers.io())
    .filter(user -> user.isActive())
    .map(User::getName)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(names -> updateUI(names));
```

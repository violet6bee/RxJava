Отчет по реализации упрощенной версии RxJava
1. Архитектура системы
1.1 Основные компоненты
Реализованная реактивная библиотека состоит из следующих ключевых компонентов:

Observable - представляет поток данных, который может обрабатывать элементы (через onNext), ошибки (через onError) и сигнал о завершении (через onComplete).
Observer - интерфейс для получения уведомлений от Observable:
public interface Observer<T> {
    void onSubscribe(Disposable d);
    void onNext(T item);
    void onError(Throwable e);
    void onComplete();
}
Disposable - механизм для управления подпиской и ресурсами:
public interface Disposable {
    void dispose();
    boolean isDisposed();
}
1.2 Принцип работы
Библиотека реализует паттерн "Наблюдатель" с добавлением реактивных возможностей:

Создание Observable с помощью фабричных методов (create, just)
Применение операторов (map, filter, flatMap)
Подписка через метод subscribe
Управление потоками выполнения через subscribeOn и observeOn
2. Schedulers: принципы работы и применение
2.1 Реализованные Schedulers
public class Schedulers {
    public static Scheduler io() {
        return new IoScheduler(); // Для IO-операций
    }
    
    public static Scheduler computation() {
        return new ComputationScheduler(); // Для вычислений
    }
}
2.2 Сравнение Schedulers
Scheduler	Пул потоков	Использование
IO	CachedThreadPool	Сетевые запросы, работа с файлами
Computation	FixedThreadPool (по ядрам CPU)	Вычислительные задачи
2.3 Пример использования
Observable.just("data")
    .subscribeOn(Schedulers.io()) // Подписка в IO-потоке
    .map(data -> process(data))   // Тяжелые вычисления
    .observeOn(Schedulers.computation()) // Обработка в вычислительном потоке
    .subscribe(result -> updateUI(result));
3. Тестирование системы
3.1 Стратегия тестирования
Модульные тесты - проверка отдельных операторов
Интеграционные тесты - проверка комбинаций операторов
Многопоточные тесты - проверка работы Schedulers
3.2 Основные тестовые сценарии
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
4. Примеры использования библиотеки
4.1 Простая цепочка операций
Observable.just("apple", "banana", "cherry")
    .filter(fruit -> fruit.length() > 5)
    .map(String::toUpperCase)
    .subscribe(
        System.out::println,
        Throwable::printStackTrace,
        () -> System.out.println("Completed!")
    );
4.2 Комбинирование операторов
Observable.range(1, 10)
    .flatMap(x -> Observable.just(x, x * 10))
    .take(5)
    .subscribe(System.out::println);
4.3 Асинхронная обработка
Observable.fromCallable(() -> fetchDataFromNetwork())
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.computation())
    .subscribe(
        data -> processData(data),
        error -> showError(error)
    );
Заключение
Реализованная библиотека предоставляет:

Основные операторы реактивного программирования
Гибкое управление потоками выполнения
Корректную обработку ошибок
Возможности композиции операторов
Пример использования в реальном приложении:

// Загрузка и обработка данных
apiService.getUsers()
    .subscribeOn(Schedulers.io())
    .filter(user -> user.isActive())
    .map(User::getName)
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(names -> updateUI(names));

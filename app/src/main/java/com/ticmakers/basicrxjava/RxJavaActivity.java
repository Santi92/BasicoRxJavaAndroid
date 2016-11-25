package com.ticmakers.basicrxjava;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class RxJavaActivity extends AppCompatActivity {

    private Button buttonAsynTasck;
    private Button buttonRxJava;
    private View rootView;
    private ProgressBar progressBar;

    CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        rootView = (View) findViewById(R.id.root_view);

        buttonAsynTasck = (Button) findViewById(R.id.start_btn1);
        buttonRxJava = (Button) findViewById(R.id.start_btn2);
        progressBar = (ProgressBar) findViewById(R.id.progreso);

        progressBar.setVisibility(View.INVISIBLE);

        buttonAsynTasck.setOnClickListener(v ->  {
            ejecutarRxAndroid();
                /*progressBar.setVisibility(View.VISIBLE);
                buttonAsynTasck.setEnabled(false);
                SampleAsyncTask asyncTask = new SampleAsyncTask();
                asyncTask.execute();*/

        });

        mCompositeSubscription = new CompositeSubscription();

        buttonRxJava.setOnClickListener(v1 -> {
            v1.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            /**
             * con lambda =)
             */
            operationObservable
                    .subscribeOn(Schedulers.io()) //SubscribeOn el hilo de E/S (Segundo plano)
                    .observeOn(mainThread()) // observeOn en la UI Thread;
                    .map(String::length)
                    .subscribe(
                            value -> Toast.makeText(this,"Valor"+value, Toast.LENGTH_SHORT).show(),
                            error -> Log.e("TAG", "Error: " + error.getMessage()),
                            () -> {v1.setEnabled(true);
                                progressBar.setVisibility(View.INVISIBLE);});
        });
    }



    /***
     * Operación
     * @return
     */
    public String longRunningOperation() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // error
        }
        return "Completo!";
    }


    private class SampleAsyncTask extends AsyncTask<Void,Void,String> {


        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(RxJavaActivity.this, result, Toast.LENGTH_LONG).show();
            buttonAsynTasck.setEnabled(true);
        }


        @Override
        protected String doInBackground(Void... params) {

            return longRunningOperation();
        }
    }

    /**
     * Creadon el observable con Lambda
     */
    final Observable<String> operationObservable = Observable.create(subscriber ->  {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // error
        }
        subscriber.onNext("Valor corto");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // error
        }
        subscriber.onNext("Valor largoooooo");




        subscriber.onCompleted();

    });

    /**
     * Creando un Observable de RxJava con Just
     * con un filtro y un map son operadores que tiene RxJava
     * tipo lambda
     */
    private void creandoObservableJust(){
        /**
         * Creamos un observable con observadores  definidos
         * tipo lambda
         */
        Log.d("VALOR","entro");
        Observable.just("First Message", "Second Message",
                "This is a large message", "Short message")
                .map(String::length)
                .filter(length -> length < 15)
                .subscribe(// OnNext
                        (valor) ->Toast.makeText(RxJavaActivity.this,"Valor : "+valor, Toast.LENGTH_LONG).show());
    }

    /**
     * Creando un observable básico y un suscritor básico
     * lambda
     */
    private void creandoObservableBasico(){

        /**
         * Crando un observable de Integer
         * lambda
         */
        Observable<Integer> integerObservable = Observable.create(suscriber ->{

            suscriber.onNext(1);
            suscriber.onNext(2);
            suscriber.onNext(3);
            suscriber.onCompleted();

        });

        /**
         * Creamos un suscritror de Integer
         * y sobre escrimos los metodos que
         * recepciona los mensajes o emisiones de observable
         */

        Subscriber<Integer> integerSubscriber = new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Complete!");
            }


            @Override
            public void onError(Throwable e) {
                System.out.println("Error" + e.getMessage());
            }


            @Override
            public void onNext(Integer o) {
                System.out.println("Complete!"+o);
            }
        };
        /**
         * Suscribimos  nuestros suscritor al observables
         */
        integerObservable.subscribe(integerSubscriber);

    }


    /**
     * Cración de un observable tipo from con RxJava y utilizando lambdas
     *
     * Se utiliza para : En caso de que queramos emitir un observable por cada objeto de una colección, como puede ser un List.
     */
    private void crearObservableFrom(){
        Integer[] numbers = {1,2,3,4,5};
        Observable.from(numbers)
                .subscribe(number -> System.out.println("Numeros " +  number));

        // Y el código equivalente es:
        Integer[] numberos = {1,2,3,4,5};
        for (Integer number : numbers) {
            System.out.println("Numero del for clasico " +  number);
        }

    }


    /**
     * Tenemos primero el operador just que nos crea un observable tipo String. Luego tenemos el
     * nuevo operador all. El operador all no emite un observable por cada item que le llega,
     * en este caso le llegarían tres: tipo, contenido y autor. Sino que emite un solo observable del tipo Boolean.
     * Este Boolean se decide con un predicado, el predicado es field != null siendo field cada uno de los item que emite just.
     * All hace un and sobre todos los resultados obtenidos y lo emite a la siguiente operación.
     */
    private void funcionalidadAll(){
        Observable.just("tipo", "contenido", "autor")
                .all(field -> field != null)
                .filter(areFieldNotNull -> areFieldNotNull  )
                .subscribe(result -> Log.d("Completo","Todos los campos no son nulo"+result));
    }

    /**
     * Esto lo podríamos imitar con RxJava usando la operación groupBy que nos agrupa los
     * ítems emitidos mediante una clave. Siendo el equivalente al anterior código este
     *
     * Primero emitimos secuencialmente cada entero almacenado en numbers con from. Luego en
     * groupBy si el número que llega desde from es par devolvemos “Even” y si es impar “Odd”.
     * Cuando todos los items desde from pasen por groupBy y no antes, groupBy emitirá tantos
     * observables como grupos haya conseguido hacer del tipo GroupedObservable<String, Integer>.
     * Para este ejemplo como solo devolvemos dos posibles valores, “Even” o “Odd” como máximo emitirá
     * dos grupos si existen al menos un número par y otro impar.
     *
     */
    private void funcionalidadGroupBy(){
        Integer[] numbers = {1,2,3,4,5};
        Observable.from(numbers)
                .groupBy(number -> number % 2 == 0 ? "Even" : "Odd")
                .subscribe(group -> {
                    group.count().subscribe(count ->
                            System.out.println(
                                    String.format("Estos son %d %s numeros", count, group.getKey()))
                    );
                });
    }


    /**
     * Contains
     * Si queremos saber si un elemento está contenido en los ítems
     * que emite un observable utilizamos contains.
     */
    private void funcionalidadContains(){
        Integer[] numbers = {1,2,3,4,5};
        Observable.from(numbers)
                .contains(5)
                .subscribe(result -> System.out.println("Es 5 in numero? " + result));
    }


    /**
     * Si para saber si un ítem está contenido en
     * los ítems que se emiten se necesita realizar algo más
     * que comparar con otro ítem utilizamos exists.
     */
    private void funcionalidadExist(){
        Observable.just("Fabio", "Marcos", "Pedro", "Elena")
                .exists(name -> name.length() > 9)
                .subscribe(result -> System.out.println("¿Existe un nombre con más de cuatro caracteres? " + result));
    }


    /**
     * Limit
     * Si necesitamos limitar el número de elementos emitidos utilizamos limit.
     */
    private void funcionalidadLimit(){
        Integer[] numbers = {1,2,3,4,5};
        Observable.from(numbers)
                .limit(3)
                .subscribe(number -> System.out.println("Numeros " + number));
    }

    /**
     * isEmpety
     * Con isEmpty podemos saber si no se emiten ítems, por ejemplo,
     * en el siguiente fragmento de código from “no emite” item debido a que numbers no tiene valores.
     * Pongo “no emite” entre comillas porque en realidad si emite un ítem pero vacío.
     */
    private void funcionalidadIsEmpety(){
        Integer[] numbers = {};
        Observable.from(numbers)
                .isEmpty()
                .subscribe(result -> System.out.println("Is array empty? " + result));
    }


    /**
     * TODO  Operaciones con varios observables
     * Link : http://sglora.com/android-tutorial-sobre-rxjava-iv/
     */

    /**
     * Concat
     * Pongamos que queremos unir las items de un observable
     * con otro, para esto utilizamos la operación concat.
     * Concat emite primero todos los ítems del primer observable
     * y luego todos los ítems del segundo observable.
     *
     *
     *
     */
    private void operacionContat(){
        Observable<Integer> firstObservable  = Observable.just(-1, 0, 1);
        Observable<Integer> secondObservable = Observable.just(3, 6, 2, 8, 5);
        Observable.concat(firstObservable, secondObservable)
                .subscribe(number -> System.out.println("Number " + number));
    }


    /**
     * SequenceEqual
     * Lo utilizamos para saber si dos observables emiten
     * secuencialmente los mismo items utilizamos
     */
    private void operacionSequenceEqual(){
        Integer[] numbers = {3,6,2,8,5};
        Observable<Integer> firstObservable  = Observable.from(numbers);
        Observable<Integer> secondObservable = Observable.just(3, 6, 2, 8, 5);

        Observable.sequenceEqual(firstObservable, secondObservable)
                .subscribe(result -> System.out.println("Es igual a la secuencia? " + result));
    }


    /**
     * CombineLatest
     * La siguiente operación nos sirve para combinar el
     * ítem más reciente emitido por el primer observable
     * con otro item emitido por otro observable.
     */

    private void operacionCambineLatest(){


        Observable<Integer> firstObservable  = Observable.just(-1, 0, 3);
        Observable<Integer> secondObservable = Observable.just(4, 6, 2, 8, 5);
        Observable.combineLatest(firstObservable, secondObservable,
                /**
                 * En este caso debemos pasar también una función para hacer la combinación
                 * como tercer parámetro de combineLatest. Para este ejemplo realizamos una
                 * suma entre el ítem más reciente de firstObservable con los ítems de secondObservable.
                 * Resultado de la suma que es emitido al siguiente observable.
                 */
                (latestNumberInFirst, numberInSecond) -> latestNumberInFirst + numberInSecond)
                .subscribe(result -> System.out.println("Sum " + result));
    }

    /**
     * Zip
     * Lo utilizamos cuando queremos  combinar dos tipos de ítems diferentes de
     * dos observables diferentes. Por ejemplos los de un primer observable que
     * emite enteros con los de un segundo observable que emite strings.
     */
    private void operacionesZip(){
        Observable<Integer> firstObservable  = Observable.just(0, 1, 2);
        Observable<String>  secondObservable = Observable.just("Primer Numero = ", "Segundo Numero = ", "Tercer Numero = ");
        /**
         * Para esto como vemos en el ejemplo utilizamos la operación zip.
         * Pasamos ambos observables como parámetros y una función que
         * defina de qué manera queremos que se haga la combinación.
         * Para este ejemplo esa función simplemente concatena a un
         * mensaje un número.
         */
        Observable.zip(firstObservable, secondObservable,
                (number, message) -> message + number)
                .subscribe(System.out::println);

    }


    /**
     * TODO Operaciones asíncronas
     *
     */

    private static Observable<Integer[]> getNumbers(){
        System.out.println("Function getNumbers Thread = " + Thread.currentThread().getName());
        return Observable.create(
                subscriber -> {
                    System.out.println("Observable.create Thread = " + Thread.currentThread().getName());
                    Integer[] numbers = {3,6,2};
                    subscriber.onNext(numbers);
                    subscriber.onCompleted();
                }
        );
    }

    /**
     * Operación de un hilo IU
     */
    private void hiloIU(){
        Thread.currentThread().setName("Main Thread");
        getNumbers()
                /**
                 * Una operación nueva que no hemos comentado hasta ahora es flatMap que es
                 * capaz a partir de una colección emitir items en streaming, items que pueden
                 * ser de diferente tipo de los que le llegan.
                 */
                .flatMap(numbers -> {
                    System.out.println("FlatMap Thread = " + Thread.currentThread().getName());
                    return Observable.from(numbers);
                })
                .map(number -> {
                    System.out.println("Map Thread = " + Thread.currentThread().getName());
                    return number % 2;
                })
                .forEach(number -> {
                    System.out.println("ForEach Thread = " + Thread.currentThread().getName());
                    System.out.println("Is Odd " + (number == 0));
                });
    }

    /**
     * Operación de un hilo
     * ObserveOn para que se ejecute en otro hilo
     */
    private void hilo(){
        Thread.currentThread().setName("Main Thread");
        getNumbers()
                .observeOn(Schedulers.newThread())
                /**
                 * Observamos que la función getNumbers y
                 * el create se ejecutan en el hilo principal
                 * debido a que preceden al observeOn. En cambio
                 * el resto de operaciones que siguen a observeOn se ejecutan en un hilo independiente.
                 */
                .flatMap(numbers -> {
                    System.out.println("FlatMap Thread = " + Thread.currentThread().getName());
                    return Observable.from(numbers);
                })
                .map(number -> {
                    System.out.println("Map Thread = " + Thread.currentThread().getName());
                    return number % 2;
                })
                .forEach(number -> {
                    System.out.println("ForEach Thread = " + Thread.currentThread().getName());
                    System.out.println("Is Odd " + (number == 0));
                });


    }

    /**
     *  SubscribeOn
     *
     * En caso de querer que incluso el create del observable se ejecute en un
     * hilo independiente usamos subscribeOn que ejecuta toda la cascada de
     * manera asíncrona.
     */
    private void hiloSubscribeOn(){
        Thread.currentThread().setName("Main Thread");
        getNumbers()
                .subscribeOn(Schedulers.newThread())
                .flatMap(numbers -> {
                    System.out.println("FlatMap Thread = " + Thread.currentThread().getName());
                    return Observable.from(numbers);
                })
                .map(number -> {
                    System.out.println("Map Thread = " + Thread.currentThread().getName());
                    return number % 2;
                })
                .toBlocking()
                .forEach(number -> {
                    System.out.println("ForEach Thread = " + Thread.currentThread().getName());
                    System.out.println("Is Odd " + (number != 0));
                });
    }


    /**
     * Amb
     *
     * La operación amb(ambiguos) emite ante los observables de
     * entrada los ítems del primer observable que llega omitiendo
     * el resto de ítems de los otros observables
     */
    private void hiloOperandoAmb(){
        Random r = new Random();
        Observable<Integer> firstObservable = Observable.just(-1, -2, -3)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextInt(2), TimeUnit.SECONDS);

        Observable<Integer> secondObservable = Observable.just(1, 2, 3)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextInt(2), TimeUnit.SECONDS);



        Observable<Integer> thirdObservable = Observable.just(4, 5, 6)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextInt(2), TimeUnit.SECONDS);

        Observable.amb(firstObservable, secondObservable,thirdObservable)
                .toBlocking()
                .forEach(number -> {
                    System.out.println("ForEach Thread = " + Thread.currentThread().getName());
                    System.out.println("Number " + number);
                });
    }


    /**
     * Merge : si emite todos los observables
     *
     * En el anterior tutorial pudimos ver el ejemplo de la operación concat
     * que ante dos o más observables emitía en orden primero todos los items
     * de un observable y luego todos los de otro observable independientemente
     * que items llegarán primero. Con merge conseguimos algo parecido pero merge
     * emite todos los items según le va llegando.
     */

    private void hiloOperacionMerge(){
        Random r = new Random();
        Observable<Integer> firstObservable = Observable.just(-1, -2, -3)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextInt(2), TimeUnit.SECONDS);
        Observable<Integer> secondObservable = Observable.just(1, 2, 3)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextInt(2), TimeUnit.SECONDS);


        Observable.merge(firstObservable, secondObservable)
                .toBlocking()
                .forEach(number -> {
                    System.out.println("ForEach Thread merge = " + Thread.currentThread().getName());
                    System.out.println("Number " + number);
                });
    }


    /**
     * AndroidSchedulers de RxAndroid
     * Aunque podemos utilizar RxJava directamente en Android
     * tenemos RxAndroid que aporta cosas especificas para Android
     * y que nos ayudan a simplificar el desarrollo. Entre otras muchas
     * cosas sobresale el AndroidSchedulers que nos permite definir en
     * el hilo de la UI para trabajar.
     */

    public Observable<User> getUser()  {
        return Observable.create(subscriber -> {
            try {
                User user = new User("Usuario"); // Realizar la petición y obtener el usuario
                subscriber.onNext(user);
                subscriber.onCompleted();
            }catch (Exception ex){
                subscriber.onError(ex);
            }
        });
    }

    private void ejecutarRxAndroid(){
        getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(user -> Log.d("TAG", "User = " + user),
                        error -> Log.e("TAG", error.getMessage(), error),
                        () -> Log.d("TAG", "Complete") );
    }


    public class User{
        private String usuario;
        public User(String usuario){
            this.usuario = usuario;
        }


        public String getUsuario() {

            return usuario;
        }
    }


    /**
     * Cancelar un suscriptor
     * Método para cancelar un suscriptor
     */
    private void cancelarSuscritor(){
        Subscription subscription =  getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe(user -> Log.d("cancelar", "User = " + user),
                        error -> Log.e("cancelar", error.getMessage(), error),
                        () -> Log.d("cancelar", "Complete") );


        subscription.unsubscribe();
    }



    /**
     * PARA CANCELAR LA SUSCRICIÓN O HILO QUE SE ESE EJECUTANDO
     * CompositeSubscription
     *
     * Descripción :
     *     Nos permirte adicionar y almacenar suscriptores
     *     creemos y poder fácilmente cancelar las suscripciones de una sola vez, puede ser
     *     un acción del usuario o en el ciclo de la vida del activity o fragment

     */
    private void cancelarSusbcritoresCompositeSubscription(){

        mCompositeSubscription.add(
                getUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(mainThread())
                        .subscribe(user -> Log.d("CompositeSubscription", "User = " + user),
                                error -> Log.e("CompositeSubscription", error.getMessage(), error),
                                () -> Log.d("CompositeSubscription", "Complete") )
        );
    }


    @Override
    public void onDestroy(){
        /**
         * Cancelamos todas las peticiones que fueron
         * almacenadas
         */
        mCompositeSubscription.unsubscribe();
        super.onDestroy();
    }


    /**
     *
     */
    private void metodoActivityLifecicle(){

    }


}

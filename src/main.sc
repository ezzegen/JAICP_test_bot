require: slotfilling/slotFilling.sc
  module = sys.zb-common
theme: /
    
    state: Start
        q!: *start
    
    state: Hello
        intent: /привет
        random:
            a: Здравствуйте! Чем могу быть полезен?
            a: Добрый день! В каком вопросе необходима моя помощь?
        script:
            // При отсутвии ответа бот закрывает сессию.
            $reactions.timeout({interval: '20s', targetState: '/Bye'});
        
    state: Password
        intent: /пароль
        a: Здравствуйте!
        a: Выберите, что именно планируете сделать:
            1. Поменять пароль для входа в приложение.
            2. Поменять PIN-код от карты.
            <b>Пожалуйста, отправьте цифру, соответствующую вашему выбору.</b>
        buttons:
            "В начало" -> /Start
        script:
            // При отсутвии ответа бот закрывает сессию.
            $reactions.timeout({interval: '20s', targetState: '/Bye'});
            
        # Обработчик ответов.
        state: ResponseHandler
            q: * (1/1./2/2./карта/приложение) *
            script: $session.value = $parseTree.text;
            if: $session.value == '1' || $session.value == 'приложение'
                go!: /ChangeAppPassword
            elseif: $session.value == '2' || $session.value == 'карта'
                go!: /ChangeCardPassword
                    
        state: LocalCatchAll
            q: *
            a: Возможно, вы ошиблись, повторите ввод.
    
    state: ChangeAppPassword || modal = true
        q: * (*мен*(~пароль/~код/*пин*/*Pin*)*~приложение) *
        a: Смена пароля возможна несколькими способами:
            1. На экране "Профиль" выберите "Изменить код входа в приложение".
            2. Введите SMS-код.
            3. Придумайте новый код для входа в приложение и повторите его.
        script:
            $reactions.timeout({interval: '2s', targetState: '/ChangeAppPassword/AnotherVariant'});
        
        state: AnotherVariant
            a: Либо нажмите на кнопку "Выйти" на странице ввода пароля для входа в приложение.
                После чего нужно будет заново пройти регистрацию:
                1. Ввести полный номер карты (если оформляли ранее, иначе номер телефона и дату рождения).
                2. Указать код из смс-код.
                3. Придумать новый пароль для входа.
            script:
                $reactions.timeout({interval: '2s', targetState: '/PoliteEnding'});
                
    state: ChangeCardPassword || modal = true
        q: * (*мен*(~пароль/~код/*пин*/*Pin*)*~карта) *
        a: Это можно сделать в приложении:
            1. На экране "Мои деньги" в разделе "Карты" нажмите на нужную.
            2. Выберите вкладку "Настройки".
            3. Нажмите "Сменить пин-код".
            4. И введите комбинацию, удобную вам.
            5. Повторите ее.
        a: И все готово! Пин-код установлен, можно пользоваться.
        go!: /PoliteEnding
                
    state: PoliteEnding
        script:
            $jsapi.stopSession();
        a: Приятно было пообщаться. Всегда готов помочь Вам снова. :-)
                
    state: Bye
        intent!: /пока
        a: Всего Вам доброго и хорошего дня! :)
        go: /Start

                    
    state: CatchAll
        event!: noMatch
        random:
            a: Я не понял. Вы сказали: {{$request.query}}
            a: Извините, я не понял, что Вы сказали... :(
        a: Попробуйте написать по-другому.
                
    state: Match
        event!: match
        a: {{$context.intent.answer}}
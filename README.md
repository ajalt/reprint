<h1 align="center">
    <img src="resources/banner.png">
</h1>

[![](https://img.shields.io/github/tag/ajalt/reprint.svg?label=maven)](https://jitpack.io/#ajalt/reprint) 
[![](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![](https://img.shields.io/badge/API-14%2B-blue.svg)
[![](https://img.shields.io/badge/javadoc-core-blue.svg)](https://jitpack.io/com/github/ajalt/reprint/core/2.5.6/javadoc/)
[![](https://img.shields.io/badge/javadoc-reactive-blue.svg)](https://jitpack.io/com/github/ajalt/reprint/reactive/2.5.6/javadoc/)

A simple, unified fingerprint authentication library for Android with
ReactiveX extensions.

* Eliminates the need to deal with the different available Fingerprint APIs, including Imprint and Samsung Pass.
* Fixes undocumented bugs and idiosyncrasies in the underlying APIs.
* Comes with help messages translated in over 80 locales that work with all APIs.
* Provides optional ReactiveX interfaces.

# Usage

See the [sample app](sample/src/main/java/com/github/ajalt/reprint/MainActivity.java) for a complete example.

In your `Application.onCreate`, initialize Reprint with
`Reprint.initialize(this)`. This will load the Marshmallow module, and the
Spass module if you included it.

Then, anywhere in your code, you can call `Reprint.authenticate` to turn on
the fingerprint reader and listen for a fingerprint. You can call
`Reprint.cancelAuthentication` to turn the reader off before it finishes
normally. 

There are two ways to be notified of authentication results: traditional
callback, and a ReactiveX Observable.

### Traditional Callbacks

Pass an `AuthenticationListener` to `authenticate`, and it's callbacks will be
called with results. The `onFailure` callback will be called repeatedly until
the sensor is disabled or a fingerprint is authenticated correctly, at which
point `onSuccess` will be called.

```java
Reprint.authenticate(new AuthenticationListener() {
    @Override
    public void onSuccess(int moduleTag) {
        showSuccess();
    }

    @Override
    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                          CharSequence errorMessage, int moduleTag, int errorCode) {
        showError(failureReason, fatal, errorMessage, errorCode);
    }
});
```

The `failureReason` is an enum value with general categories of reason that
the authentication failed. This is useful for displaying help messages in your
UI.

The `fatal` parameter is true if the sensor has stopped and cannot be
restarted, such as when it's locked out. It's false if the sensor is still
running, such as when a finger was moved over the sensor too quickly.

The `errorMessage` is a string that will contain some help text provided by
the underlying SDK about the failure. You should show this text to the user,
or some other message of your own based on the failureReason. This string will
never be null, and will be localized into the current locale.

The `moduleTag` and `errorCode` can be used to find out the SDK-specific
reason for the failure.

### ReactiveX interface

If you include the `reactive` reprint library, you can be notified of
authentication results through an Observable by calling
`RxReprint.authenticate`. In this case, the subscriber's `onNext` will be
called at most once, after a successful authentication. When the `onError`
method is called, the sensor will already be stopped.

```java
RxReprint.authenticate().subscribe(::showSuccess, ::showError);
```

You probably want to use the `retry` operator to restart the sensor when a
non-fatal error occurs. The `RxReprint.retryNonFatal` method takes care of the
most common use case.

```java
RxReprint.authenticate()
         .doOnError(::showHelp)
         .retry(RxReprint.retryNonFatal(5))
         .subscribe(::showSuccess, ::showError);
```

One advantage that this interface has is that when the subscriber
unsubscribes, the authentication request is automatically canceled. So you
could, for example, use the
[RxLifecycle](https://github.com/trello/RxLifecycle) library to bind the
observable, and the authentication will be canceled when your activity
pauses.

# Installation

Reprint is distributed with [jitpack](https://jitpack.io/) and split up into
several libraries, so you can include only the parts that you use.

First, add jitpack to your gradle repositories.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

Then add the core library and optionally the Samsung Pass interface and the
ReactiveX interface.

```groovy
dependencies {
   compile 'com.github.ajalt.reprint:core:2.5.6@aar' // required, supports marshmallow devices
   compile 'com.github.ajalt.reprint:reprint_spass:2.5.6@aar' // optional: support for pre-marshmallow Samsung devices
   compile 'com.github.ajalt.reprint:reactive:2.5.6@aar' // optional: the ReactiveX interface
}
```

If you use other libraries requiring appcompat-v7 make sure to exclude them :
```groovy
compile ('com.github.ajalt.reprint:core:2.5.6@aar'){
        exclude group: 'com.android.support', module: 'appcompat-v7'
}
compile ('com.github.ajalt.reprint:reprint_spass:2.5.6@aar'){
        exclude group: 'com.android.support', module: 'appcompat-v7'
}
```

### Permissions

Reprint requires the following permissions be declared in your `AndroidManifest.xml`

```xml
<!-- Marshmallow fingerprint permission-->
<uses-permission android:name="android.permission.USE_FINGERPRINT"/>

<!-- Samsung fingerprint permission, only required if you include the Spass module -->
<uses-permission android:name="com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY"/>
```

# License

    Copyright 2015 AJ Alt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

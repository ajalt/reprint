# Deprecated

Use [androidx.biometric](https://developer.android.com/jetpack/androidx/releases/biometric) instead, which supports other forms of biometric authentication such as iris scanning an facial recognition, and provides a UI that is consistent across applications.

<h1 align="center">
    <img src="resources/banner.png">
</h1>

A simple, unified fingerprint authentication library for Android with
RxJava extensions.

* Eliminates the need to deal with the different available Fingerprint APIs, including Imprint and Samsung Pass.
* Fixes undocumented bugs and idiosyncrasies in the underlying APIs.
* Supports more Imprint devices than FingerprintManagerCompat from the androidx library.
* Comes with help messages translated in over 80 locales that work with all APIs.
* Provides optional RxJava interfaces.

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

### RxJava interface

If you include the `reactive` reprint library, you can be notified of
authentication results through an `Observable` (or `Flowable` with RxJava 2) by
calling `RxReprint.authenticate`. In this case, the subscriber's `onNext` will
be called after each failure and after success.

```java
RxReprint.authenticate()
    .subscribe(result -> {
        switch (result.status) {
            case SUCCESS:
                showSuccess();
                break;
            case NONFATAL_FAILURE:
                showHelp(result.failureReason, result.errorMessage);
                break;
            case FATAL_FAILURE:
                showError(result.failureReason, result.errorMessage);
                break;
        }
    });
```

The `failureReason` is an enum value with general categories of reason that
the authentication failed. This is useful for displaying custom help messages in your
UI.

The `errorMessage` is a string that will contain some help text provided by
the underlying SDK about the failure. You should show this text to the user,
or some other message of your own based on the `failureReason`. This string will
never be null from a failure, and will be localized into the current locale.

For detail on the other parameters,
[see the Javadocs](https://jitpack.io/com/github/ajalt/reprint/rxjava/3.3.2/javadoc/).

One advantage that this interface has is that when the subscriber unsubscribes,
the authentication request is automatically canceled. So you could, for example,
use the [RxLifecycle](https://github.com/trello/RxLifecycle) library to bind the
observable, and the authentication will be canceled when your activity pauses.

### Traditional Callbacks

If you want to use Reprint without RxJava, you can pass an
`AuthenticationListener` to `authenticate`. The `onFailure` callback will be
called repeatedly until the sensor is disabled or a fingerprint is authenticated
correctly, at which point `onSuccess` will be called.

```java
Reprint.authenticate(new AuthenticationListener() {
    public void onSuccess(int moduleTag) {
        showSuccess();
    }

    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                          CharSequence errorMessage, int moduleTag, int errorCode) {
        showError(failureReason, fatal, errorMessage, errorCode);
    }
});
```

# Documentation

The javadocs for the Reprint modules are available online:

 * [Reprint core](https://jitpack.io/com/github/ajalt/reprint/core/3.3.2/javadoc/index.html?com/github/ajalt/reprint/core/Reprint.html)
 * [RxJava 1 interface](https://jitpack.io/com/github/ajalt/reprint/rxjava/3.3.2/javadoc/com/github/ajalt/reprint/rxjava/RxReprint.html)
 * [RxJava 2 interface](https://jitpack.io/com/github/ajalt/reprint/rxjava2/3.3.2/javadoc/com/github/ajalt/reprint/rxjava2/RxReprint.html)

# Installation

Reprint is distributed with [jitpack](https://jitpack.io/#ajalt/reprint) and split up into
several libraries, so you can include only the parts that you use.

First, add Jitpack to your gradle repositories.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

Then add the core library and optionally the Samsung Pass interface and the
ReactiveX interface. Reprint provides support for both RxJava 1 and 2; you should
include the module that matches the version of RxJava that you use in your project.

```groovy
dependencies {
   compile 'com.github.ajalt.reprint:core:3.3.2@aar' // required: supports marshmallow devices
   compile 'com.github.ajalt.reprint:reprint_spass:3.3.2@aar' // optional: deprecated support for pre-marshmallow Samsung devices
   compile 'com.github.ajalt.reprint:rxjava:3.3.2@aar' // optional: the RxJava 1 interface
   compile 'com.github.ajalt.reprint:rxjava2:3.3.2@aar' // optional: the RxJava 2 interface
}
```

### Permissions

Reprint requires the following permissions be declared in your
`AndroidManifest.xml`. As long as you use the `aar` artifacts, these permissions
will be included automatically.

```xml
<!-- Marshmallow fingerprint permission-->
<uses-permission android:name="android.permission.USE_FINGERPRINT"/>

<!-- Samsung fingerprint permission, only required if you include the Spass module -->
<uses-permission android:name="com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY"/>
```

### Spass SDK deprecation

Samsung has deprecated the Spass SDK in favor of the standard Android APIs. Although Reprint still provides
a module that uses the the Spass SDK if the standard APIs aren't available, you should be aware that the Spass
SDK has a [known bug](https://github.com/ajalt/reprint/issues/6). If you don't need fingerprint support on
devices running KitKat, you should not include the `reprint_spass` module.

# License

    Copyright 2015-2019 AJ Alt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

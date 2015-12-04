# Reprint [![Release](https://img.shields.io/github/tag/ajalt/reprint.svg?label=maven)](https://jitpack.io/#ajalt/reprint)

A simple, unified fingerprint authentication library for Android with
ReactiveX extensions.

Fingerprint authentication on Android is a mess. This library unifies the
different available APIs, deals with the idiosyncrasies of each, and adds an
optional reactive interface.

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
   compile 'com.github.ajalt.reprint:core:1.1.1@aar' // required, supports marshmallow devices
   compile 'com.github.ajalt.reprint:reprint_spass:1.1.1@aar' // optional: support for pre-marshmallow Samsung devices
   compile 'com.github.ajalt.reprint:reactive:1.1.1@aar' // optional: the ReactiveX interface
}
``` 

### Permissions

Reprint requires the following permissions be declared in your `AndroidManifest.xml`

```xml
<!-- Marshmallow fingerprint permission-->
<uses-permission android:name="android.permission.USE_FINGERPRINT"/>

<!-- Samsung fingerprint permission, only reqiured if you include the spass module -->
<uses-permission android:name="com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY"/>
```

# Usage

See the [sample app](sample/src/main/java/com/github/ajalt/reprint/MainActivity.java) for a complete example.

In your `Application.onCreate`, initialize Reprint with
`Reprint.initialize(this)`. This will load the Marshmallow module, and the
Spass module if you included it.

Then, anywhere in your code, you can call `Reprint.authenticate` to turn on
the fingerprint reader and listen for a fingerprint. You can call
`Reprint.cancelAuthentication` to turn the reader off before it finishes
normally. 

There are two ways to be notified of authentication results.

### Callbacks

Pass an `AuthenticationListener` to `authenticate`, and it's callbacks will be
called with results.

```java
Reprint.authenticate(new AuthenticationListener() {
    @Override
    public void onSuccess() {
        showSuccess();
    }

    @Override
    public void onFailure(@NonNull AuthenticationFailureReason failureReason, boolean fatal,
                          @Nullable CharSequence errorMessage, int moduleTag, int errorCode) {
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

The `errorMessage` is a string that, if it's not null, will contain some help
text provided by the underlying SDK about the failure. You should show this
text to the user. The underlying SDK might not provide help text, in which
case you should show your own message.

The `moduleTag` and `errorCode` can be used to find out the SDK-specific
reason for the failure.

### ReactiveX interface

If you include the `reactive` reprint library, you can be notified of
authentication results through an Observable by calling `RxReprint.authenticate`.

```java
RxReprint.authenticate()
    .subscribe(r -> {
        if (r.failureReason == null) {
            showSuccess();
        } else {
            showError(r.failureReason, r.fatal, r.errorMessage, r.errorCode);
        }
    });
```

The Subscriber's `onNext` will be called for both success and failure, since
failure doesn't necessarily end the event stream. The subscriber is given a
data class, `AuthenticationResult`, which contains the fields that match the
parameters to the `onFailure` method listed above. 

If the `failureReason` is null, then the authentication was a success.
Otherwise, the fields have the same values as above.

One advantage that this interface has is that when the subscriber
unsubscribes, the authentication request is automatically canceled. So you
could, for example, use the
[RxLifecycle](https://github.com/trello/RxLifecycle) library to bind the
observable, and the authentication will be canceled when your activity
pauses.

# License

    Copyright (c) 2015 AJ Alt


    Permission is hereby granted, free of charge, to any person obtaining
    a copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:

    The above copyright notice and this permission notice shall be included
    in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
    CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
    TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
    SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

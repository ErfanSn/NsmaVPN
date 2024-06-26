/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.erfansn.nsmavpn.core.initializer

import android.content.Context
import androidx.startup.Initializer
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R

class SentryInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        SentryAndroid.init(context) { options ->
            options.setDsn(context.getString(R.string.sentry_dsn))
            options.setBeforeSend { event, _ ->
                if (SentryLevel.DEBUG == event.level) null else event
            }

            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true

            options.tracesSampleRate = 1.0

            options.isEnableAutoActivityLifecycleTracing = true
            options.isEnableActivityLifecycleBreadcrumbs = true

            options.environment = "production"

            options.isEnabled = !BuildConfig.DEBUG
        }
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}

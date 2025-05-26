package test.primo.primofeedapp.core.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidResourceProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AndroidResourceProvider {

    override fun getStringFromAssert(fileName: String): String? {
        return try {
            context.assets.open(fileName)
                .bufferedReader().use {
                    it.readText()
                }
        } catch (e: Exception) {
            return null
        }
    }
}
package com.example.myshoppinglist.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myshoppinglist.data.ShopListRepositoryImpl
import com.example.myshoppinglist.domain.AddShopItemUseCase
import com.example.myshoppinglist.domain.EditShopItemUseCase
import com.example.myshoppinglist.domain.GetShopItemUseCase
import com.example.myshoppinglist.domain.ShopItem
import kotlinx.coroutines.launch

class ShopItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShopListRepositoryImpl(application)

    private val getShopItemUseCase = GetShopItemUseCase(repository)
    private val addShopItemUseCase = AddShopItemUseCase(repository)
    private val editShopItemUseCase = EditShopItemUseCase(repository)

    private val _errorInputName = MutableLiveData<Boolean>() /*можно работать с этой переменной из
    ViewModel и можно устанавливать значения*/
    val errorInputName: LiveData<Boolean> // из Activity будем подписываться на эту переменную
        get() = _errorInputName // переопределяем get-ер, который будет возвращать значение перемен-
    // ной _errorInputName

    private val _errorInputCount = MutableLiveData<Boolean>()
    val errorInputCount: LiveData<Boolean>
        get() = _errorInputCount

    private val _shopItem = MutableLiveData<ShopItem>()
    val shopItem: LiveData<ShopItem>
        get() = _shopItem

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean>
        get() = _shouldCloseScreen


    fun getShopItem(shopItemId: Int) { //принимает в качестве параметра shopItemId
        viewModelScope.launch {
            val item = getShopItemUseCase.getShopItem(shopItemId) // получаем элемент
            _shopItem.value = item // устанавлием его в LiveData
        }
    }

    fun addShopItem(inputName: String?, inputCount: String?) {
        val name = parseName(inputName)
        val count = parseCount(inputCount)
        val fieldsVailed = validateInput(name, count)
        if (fieldsVailed) { // если поля валидные, то добаялем новый элемент
            viewModelScope.launch {
                val shopItem = ShopItem(name, count, true) // вот он новый элемент
                addShopItemUseCase.addShopItem(shopItem)
                finishWork()
            }
        }
    }

    fun editShopItem(inputName: String?, inputCount: String?) {

        val name = parseName(inputName)
        val count = parseCount(inputCount)
        val fieldsVailed = validateInput(name, count)
        if (fieldsVailed) {
            _shopItem.value?.let {
                viewModelScope.launch {
                    val item = it.copy(name = name, count = count)
                    editShopItemUseCase.editShopItem(item)
                    finishWork()
                }
            }
        }
    }

    private fun parseName(inputName: String?): String { //приводим строку ввода в нормальный вид
        //принимает нулабельный тип, а возвращает ненулабельную строку
        return inputName?.trim() ?: "" // если inputName не null, то обрезаем пробелы, если null,то
        // возвращаем пустую строку
    }

    private fun parseCount(inputCount: String?): Int {
        return try {
            inputCount?.trim()?.toInt() ?: 0 // ?: - это элвис-оператор, а 0 - это значение по
            // умолчанию, если в строку ничего не ввели
        } catch (e: Exception) { // в случае ошибки тоже 0
            0
        }
    }

    private fun validateInput(name: String, count: Int): Boolean { // проводим валидацию
        var result = true
        if (name.isBlank()) {
            result = false
            _errorInputName.value = true
        }
        if (count <= 0) {
            result = false
            _errorInputCount.value = true
        }
        return result
    }

    fun resetErrorInputName() {
        _errorInputName.value = false
    }

    fun resetErrorInputCount() {
        _errorInputCount.value = false
    }

    private fun finishWork() {
        _shouldCloseScreen.value = true
    }
}
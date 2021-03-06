/*
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.op.stages.sparkwrappers.specific

import com.salesforce.op.features.types.{OPVector, Prediction, RealNN}
import com.salesforce.op.stages.base.binary.OpTransformer2
import com.salesforce.op.stages.impl.classification._
import com.salesforce.op.stages.impl.regression._
import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostRegressionModel}
import org.apache.spark.ml.classification._
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.regression._
import org.apache.spark.ml.{Model, PredictionModel}
import ml.combust.mleap.runtime.transformer.classification.{LogisticRegression => MlLogisticRegression}
import ml.combust.mleap.runtime.transformer.classification.{RandomForestClassifier => MlRandomForestClassifier}
import ml.combust.mleap.runtime.transformer.classification.{NaiveBayesClassifier => MlNaiveBayesClassifier}
import ml.combust.mleap.runtime.transformer.classification.{DecisionTreeClassifier => MlDecisionTreeClassifier}
import ml.combust.mleap.runtime.transformer.classification.{GBTClassifier => MlGBTClassifier}
import ml.combust.mleap.runtime.transformer.classification.{LinearSVC => MlLinearSVC}
import ml.combust.mleap.runtime.transformer.classification.{MultiLayerPerceptronClassifier => MlMultiLayerPerceptronClassifier}
import ml.combust.mleap.runtime.transformer.regression.{LinearRegression => MlLinearRegression}
import ml.combust.mleap.runtime.transformer.regression.{RandomForestRegression => MlRandomForestRegression}
import ml.combust.mleap.runtime.transformer.regression.{GeneralizedLinearRegression => MlGeneralizedLinearRegression}
import ml.combust.mleap.runtime.transformer.regression.{DecisionTreeRegression => MlDecisionTreeRegression}
import ml.combust.mleap.runtime.transformer.regression.{GBTRegression => MlGBTRegression}
import ml.combust.mleap.xgboost.runtime.{XGBoostClassification => MlXGBoostClassification}
import ml.combust.mleap.xgboost.runtime.{XGBoostRegression => MlXGBoostRegression}




/**
 * Allows conversion from spark models to models that follow the OP convention of having a
 * transformFn that can be called on a single row rather than the whole dataframe
 */
object SparkModelConverter {

  /**
   * Converts supported spark model of type PredictionModel[Vector, T] to an OP model
   * @param model model to convert
   * @param uid uid to give converted model
   * @tparam T type of model to convert
   * @return Op Binary Model which will produce the same values put into a Prediction return feature
   */
  def toOP[T <: PredictionModel[Vector, T]](
    model: T,
    uid: String
  ): OpPredictorWrapperModel[T] = {
    toOPUnchecked(model, uid).asInstanceOf[OpPredictorWrapperModel[T]]
  }

  /**
   * Converts supported spark model of type PredictionModel[Vector, T] to an OP model
   * @param model model to convert
   * @param uid uid to give converted model
   * @return Op Binary Model which will produce the same values put into a Prediction return feature
   */
  def toOPUnchecked(
    model: Any,
    uid: String
  ): OpTransformer2[RealNN, OPVector, Prediction] = {
    model match {
      case m: LogisticRegressionModel => new OpLogisticRegressionModel(m, uid = uid)
      case m: MlLogisticRegression => new OpLogisticRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: RandomForestClassificationModel => new OpRandomForestClassificationModel(m, uid = uid)
      case m: MlRandomForestClassifier =>
        new OpRandomForestClassificationModel(null, uid = uid).setLocalMlStage(m)
      case m: NaiveBayesModel => new OpNaiveBayesModel(m, uid)
      case m: MlNaiveBayesClassifier => new OpNaiveBayesModel(null, uid = uid).setLocalMlStage(m)
      case m: DecisionTreeClassificationModel => new OpDecisionTreeClassificationModel(m, uid = uid)
      case m: MlDecisionTreeClassifier =>
        new OpDecisionTreeClassificationModel(null, uid = uid).setLocalMlStage(m)
      case m: GBTClassificationModel => new OpGBTClassificationModel(m, uid = uid)
      case m: MlGBTClassifier => new OpGBTClassificationModel(null, uid = uid).setLocalMlStage(m)
      case m: LinearSVCModel => new OpLinearSVCModel(m, uid = uid)
      case m: MlLinearSVC => new OpLinearSVCModel(null, uid = uid).setLocalMlStage(m)
      case m: MultilayerPerceptronClassificationModel => new OpMultilayerPerceptronClassificationModel(m, uid = uid)
      case m: MlMultiLayerPerceptronClassifier =>
        new OpMultilayerPerceptronClassificationModel(null, uid = uid).setLocalMlStage(m)
      case m: LinearRegressionModel => new OpLinearRegressionModel(m, uid = uid)
      case m: MlLinearRegression => new OpLinearRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: RandomForestRegressionModel => new OpRandomForestRegressionModel(m, uid = uid)
      case m: MlRandomForestRegression => new OpRandomForestRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: GBTRegressionModel => new OpGBTRegressionModel(m, uid = uid)
      case m: MlGBTRegression => new OpGBTRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: DecisionTreeRegressionModel => new OpDecisionTreeRegressionModel(m, uid = uid)
      case m: MlDecisionTreeRegression => new OpDecisionTreeRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: GeneralizedLinearRegressionModel => new OpGeneralizedLinearRegressionModel(m, uid = uid)
      case m: MlGeneralizedLinearRegression =>
        new OpGeneralizedLinearRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m: XGBoostClassificationModel => new OpXGBoostClassificationModel(m, uid = uid)
      case m: MlXGBoostClassification => new OpXGBoostClassificationModel(null, uid = uid).setLocalMlStage(m)
      case m: XGBoostRegressionModel => new OpXGBoostRegressionModel(m, uid = uid)
      case m: MlXGBoostRegression => new OpXGBoostRegressionModel(null, uid = uid).setLocalMlStage(m)
      case m => throw new RuntimeException(s"model conversion not implemented for model $m")
    }
  }

}
